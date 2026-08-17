#!/usr/bin/env bash
#
# run.sh - compila e executa os testes JUnit 5 deste projeto.
#
# O projeto e' um projeto Eclipse puro (.classpath/.project), sem Maven/Gradle,
# entao este script localiza o JDK e os jars do JUnit que vem embutidos nas
# extensoes Java do VSCode.
#
# Uso:
#   ./run.sh                      roda todos os testes
#   ./run.sh CalculadoraTest      roda uma classe (nome simples ou pacote.Classe)
#   ./run.sh calculadora          roda todos os testes de um pacote
#   ./run.sh -k                   ignora arquivos que nao compilam e roda o resto
#   ./run.sh -h                   ajuda
#
# Variaveis de ambiente:
#   JAVA_HOME       JDK a usar (senao: javac do PATH, senao o JDK do VSCode)
#   SRC_ENCODING    encoding dos fontes (padrao: ISO-8859-1)

set -euo pipefail

PROJ="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD="$PROJ/.build"
CLASSES="$BUILD/classes"
RUNNER="$BUILD/runner"
ENC="${SRC_ENCODING:-ISO-8859-1}"

if [[ -t 1 ]]; then
  RED=$'\e[31m'; GRN=$'\e[32m'; YLW=$'\e[33m'; BLD=$'\e[1m'; DIM=$'\e[2m'; RST=$'\e[0m'
else
  RED=''; GRN=''; YLW=''; BLD=''; DIM=''; RST=''
fi

die() { echo "${RED}erro:${RST} $*" >&2; exit 1; }
info() { echo "${DIM}==>${RST} $*"; }

# Imprime o cabecalho deste arquivo, do titulo ate' o fim do bloco de comentario.
usage() { awk 'NR>2 && /^#/ {sub(/^# ?/, ""); print; next} NR>2 {exit}' "${BASH_SOURCE[0]}"; exit 0; }

KEEP_GOING=0
QUERIES=()
while (($#)); do
  case "$1" in
    -h|--help)       usage ;;
    -k|--keep-going) KEEP_GOING=1 ;;
    -*)              die "opcao desconhecida: $1 (use -h)" ;;
    *)               QUERIES+=("$1") ;;
  esac
  shift
done

# ---------------------------------------------------------------- dependencias

# Editores onde procurar as extensoes Java, em ordem de preferencia.
EXT_ROOTS=("$HOME/.vscode/extensions" "$HOME/.vscode-server/extensions"
           "$HOME/.cursor/extensions" "$HOME/.antigravity/extensions")

# newest <glob> -> caminho de maior versao, no primeiro editor que tiver algum.
newest() {
  local root p; local -a hits
  for root in "${EXT_ROOTS[@]}"; do
    hits=()
    for p in "$root"/$1; do [[ -e "$p" ]] && hits+=("$p"); done
    ((${#hits[@]})) && { printf '%s\n' "${hits[@]}" | sort -V | tail -1; return 0; }
  done
  return 1
}

if [[ -n "${JAVA_HOME:-}" && -x "$JAVA_HOME/bin/javac" ]]; then
  JAVAC="$JAVA_HOME/bin/javac"; JAVA="$JAVA_HOME/bin/java"
elif command -v javac >/dev/null 2>&1 && command -v java >/dev/null 2>&1; then
  JAVAC="$(command -v javac)"; JAVA="$(command -v java)"
elif JAVAC="$(newest 'redhat.java-*/jre/*/bin/javac')"; then
  JAVA="${JAVAC%/javac}/java"
else
  die "nenhum JDK encontrado. Instale um JDK ou defina JAVA_HOME."
fi

JUNIT_DIR="$(newest 'vscjava.vscode-java-test-*/server')" \
  || die "jars do JUnit nao encontrados. Instale a extensao 'Test Runner for Java' no VSCode."

# Escolhe a versao mais nova que casa com o padrao.
pick() { { ls -1 "$JUNIT_DIR"/$1 2>/dev/null || true; } | sort -V | tail -1; }

# JUnit 5 usa jupiter 5.x + platform 1.x; JUnit 6 usa 6.x nos dois.
# Nao da' pra misturar as familias, entao fixamos uma.
if [[ -n "$(pick 'junit-jupiter-api_5.*.jar')" ]]; then JUP='5'; PLAT='1'; else JUP='6'; PLAT='6'; fi

CP=""
for pat in "junit-jupiter-api_$JUP.*.jar" "junit-jupiter-engine_$JUP.*.jar" \
           "junit-jupiter-params_$JUP.*.jar" "junit-platform-commons_$PLAT.*.jar" \
           "junit-platform-engine_$PLAT.*.jar" "junit-platform-launcher_$PLAT.*.jar" \
           "org.apiguardian.api_*.jar" "org.opentest4j_*.jar"; do
  jar="$(pick "$pat")"
  [[ -n "$jar" ]] || die "jar nao encontrado em $JUNIT_DIR: $pat"
  CP="$CP:$jar"
done

# Hamcrest (usado por ProdutoTest) vem junto do language server; opcional.
if ham="$(newest 'redhat.java-*/server/plugins/org.hamcrest_*.jar')"; then
  CP="$CP:$ham"
fi

CP="${CP#:}"

# ---------------------------------------------------------------- compilacao

# Zera as classes a cada execucao: senao um .class de um fonte renomeado,
# apagado ou ignorado por -k continua sendo descoberto e executado.
rm -rf "$CLASSES"
mkdir -p "$CLASSES" "$RUNNER"
mapfile -t SOURCES < <(find "$PROJ/src" "$PROJ/test" -name '*.java' 2>/dev/null | sort)
((${#SOURCES[@]})) || die "nenhum arquivo .java encontrado em src/ e test/"

ERRLOG="$BUILD/javac.log"
info "compilando ${#SOURCES[@]} arquivos (encoding $ENC) com $("$JAVAC" -version 2>&1)"

compile() { "$JAVAC" -nowarn -encoding "$ENC" -cp "$CP" -d "$CLASSES" "$@" 2>"$ERRLOG"; }

if ! compile "${SOURCES[@]}"; then
  if ((KEEP_GOING)); then
    # Remove os arquivos que falharam e tenta de novo. Repete porque excluir um
    # fonte pode quebrar quem dependia dele.
    excluded=()
    for _ in 1 2 3 4 5; do
      mapfile -t bad < <(sed -n 's/^\(.*\.java\):[0-9]*: error:.*/\1/p' "$ERRLOG" | sort -u)
      ((${#bad[@]})) || break
      for b in "${bad[@]}"; do
        excluded+=("$b")
        for i in "${!SOURCES[@]}"; do
          [[ "${SOURCES[$i]}" == "$b" ]] && unset 'SOURCES[i]'
        done
      done
      SOURCES=("${SOURCES[@]}")
      ((${#SOURCES[@]})) || die "nenhum arquivo compilavel restou"
      compile "${SOURCES[@]}" && break
    done
    echo "${YLW}aviso:${RST} ignorando ${#excluded[@]} arquivo(s) que nao compilam:"
    printf '  %s\n' "${excluded[@]#$PROJ/}"
  else
    cat "$ERRLOG" >&2
    echo >&2
    die "a compilacao falhou. Corrija os erros acima, ou use ./run.sh -k para rodar o resto."
  fi
fi

# ---------------------------------------------------------------- runner

# O junit-platform-console-standalone.jar nao vem com as extensoes do VSCode,
# entao geramos um launcher minimo com a API do junit-platform-launcher.
if [[ ! -f "$RUNNER/Runner.class" || "${BASH_SOURCE[0]}" -nt "$RUNNER/Runner.class" ]]; then
  cat > "$RUNNER/Runner.java" <<'JAVA'
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.*;
import org.junit.platform.engine.DiscoverySelector;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.*;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

/** Descobre e executa testes. args[0] = diretorio de classes; demais = seletores. */
public class Runner {
    public static void main(String[] args) {
        Path root = Path.of(args[0]);
        List<DiscoverySelector> selectors = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("class:")) selectors.add(DiscoverySelectors.selectClass(a.substring(6)));
            else if (a.startsWith("package:")) selectors.add(DiscoverySelectors.selectPackage(a.substring(8)));
        }
        if (selectors.isEmpty()) selectors.addAll(DiscoverySelectors.selectClasspathRoots(Set.of(root)));

        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectors).build();

        SummaryGeneratingListener summary = new SummaryGeneratingListener();
        LauncherFactory.create().execute(request, summary, new Printer());

        PrintWriter out = new PrintWriter(System.out);
        System.out.println();
        summary.getSummary().printTo(out);
        summary.getSummary().printFailuresTo(out, 6);
        out.flush();
        System.exit(summary.getSummary().getTotalFailureCount() > 0 ? 1 : 0);
    }

    /** Imprime uma linha por teste, conforme cada um termina. */
    static class Printer implements TestExecutionListener {
        private String lastClass = "";

        @Override public void executionFinished(TestIdentifier id, TestExecutionResult result) {
            if (!id.isTest()) return;
            String mark = switch (result.getStatus()) {
                case SUCCESSFUL -> "\033[32m PASSOU \033[0m";
                case FAILED     -> "\033[31m FALHOU \033[0m";
                case ABORTED    -> "\033[33m ABORT  \033[0m";
            };
            print(id, mark);
            result.getThrowable().ifPresent(t -> System.out.println("           " + t));
        }

        @Override public void executionSkipped(TestIdentifier id, String reason) {
            if (id.isTest()) print(id, "\033[2m PULOU  \033[0m");
        }

        private void print(TestIdentifier id, String mark) {
            String cls = id.getSource()
                    .filter(s -> s instanceof MethodSource)
                    .map(s -> ((MethodSource) s).getClassName())
                    .orElse("");
            if (!cls.equals(lastClass)) {
                lastClass = cls;
                System.out.println("\n\033[1m" + cls + "\033[0m");
            }
            System.out.println(mark + " " + id.getDisplayName());
        }
    }
}
JAVA
  "$JAVAC" -nowarn -encoding UTF-8 -cp "$CP" -d "$RUNNER" "$RUNNER/Runner.java" \
    || die "falha ao compilar o runner interno"
fi

# ---------------------------------------------------------------- seletores

# Traduz "CalculadoraTest" / "calculadora" / "calculadora.CalculadoraTest"
# no seletor que o Runner entende.
SELECTORS=()
for q in "${QUERIES[@]:-}"; do
  [[ -n "$q" ]] || continue
  path="${q//.//}"
  if [[ -f "$CLASSES/$path.class" ]]; then
    SELECTORS+=("class:$q")
  elif [[ -d "$CLASSES/$path" ]]; then
    SELECTORS+=("package:$q")
  else
    hit="$(cd "$CLASSES" && find . -name "$q.class" | head -1)"
    [[ -n "$hit" ]] || die "nao encontrei a classe nem o pacote '$q' (ja' compilou?)"
    fqn="${hit#./}"; fqn="${fqn%.class}"
    SELECTORS+=("class:${fqn//\//.}")
  fi
done

((${#SELECTORS[@]})) && info "rodando: ${SELECTORS[*]}" || info "rodando todos os testes"

exec "$JAVA" -cp "$RUNNER:$CLASSES:$CP" Runner "$CLASSES" "${SELECTORS[@]:-}"
