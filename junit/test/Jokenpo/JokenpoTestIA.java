package jokenpo;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Testes do metodo Main.jogar(int, int).
 *
 * Regras do Jokenpo conforme a documentacao do metodo:
 * escolhas 1 (papel), 2 (pedra) e 3 (tesoura);
 * retorno 0 (empate), 1 (jogador 1 venceu), 2 (jogador 2 venceu)
 * e -1 (opcao invalida).
 *
 * Arquivo sem caracteres acentuados de proposito: o build compila os fontes
 * com encoding ISO-8859-1 (ver run.sh).
 */
@DisplayName("Jokenpo - Main.jogar()")
public class JokenpoTestIA {

    private static final int PAPEL = 1;
    private static final int PEDRA = 2;
    private static final int TESOURA = 3;

    private static final int EMPATE = 0;
    private static final int VENCEU_JOGADOR_1 = 1;
    private static final int VENCEU_JOGADOR_2 = 2;
    private static final int INVALIDO = -1;

    private Main jogo;

    @BeforeEach
    public void inicializa() {
        jogo = new Main();
    }

    @Nested
    @DisplayName("Empates")
    class Empates {

        @ParameterizedTest(name = "jogar({0}, {0}) deve empatar")
        @ValueSource(ints = { PAPEL, PEDRA, TESOURA })
        @DisplayName("Escolhas iguais sempre empatam")
        public void escolhasIguaisEmpatam(int escolha) {
            assertEquals(EMPATE, jogo.jogar(escolha, escolha));
        }
    }

    @Nested
    @DisplayName("Vitorias do jogador 1")
    class VitoriasJogador1 {

        @Test
        @DisplayName("Papel cobre pedra")
        public void papelVencePedra() {
            assertEquals(VENCEU_JOGADOR_1, jogo.jogar(PAPEL, PEDRA));
        }

        @Test
        @DisplayName("Pedra quebra tesoura")
        public void pedraVenceTesoura() {
            assertEquals(VENCEU_JOGADOR_1, jogo.jogar(PEDRA, TESOURA));
        }

        @Test
        @DisplayName("Tesoura corta papel")
        public void tesouraVencePapel() {
            assertEquals(VENCEU_JOGADOR_1, jogo.jogar(TESOURA, PAPEL));
        }
    }

    @Nested
    @DisplayName("Vitorias do jogador 2")
    class VitoriasJogador2 {

        @Test
        @DisplayName("Pedra do jogador 1 perde para o papel do jogador 2")
        public void pedraPerdeParaPapel() {
            assertEquals(VENCEU_JOGADOR_2, jogo.jogar(PEDRA, PAPEL));
        }

        @Test
        @DisplayName("Tesoura do jogador 1 perde para a pedra do jogador 2")
        public void tesouraPerdeParaPedra() {
            assertEquals(VENCEU_JOGADOR_2, jogo.jogar(TESOURA, PEDRA));
        }

        @Test
        @DisplayName("Papel do jogador 1 perde para a tesoura do jogador 2")
        public void papelPerdeParaTesoura() {
            assertEquals(VENCEU_JOGADOR_2, jogo.jogar(PAPEL, TESOURA));
        }
    }

    @Nested
    @DisplayName("Matriz completa de jogadas validas")
    class MatrizCompleta {

        @ParameterizedTest(name = "jogar({0}, {1}) == {2}")
        @CsvSource({
                // jogador1, jogador2, esperado
                "1, 1, 0", // papel   x papel   -> empate
                "1, 2, 1", // papel   x pedra   -> jogador 1
                "1, 3, 2", // papel   x tesoura -> jogador 2
                "2, 1, 2", // pedra   x papel   -> jogador 2
                "2, 2, 0", // pedra   x pedra   -> empate
                "2, 3, 1", // pedra   x tesoura -> jogador 1
                "3, 1, 1", // tesoura x papel   -> jogador 1
                "3, 2, 2", // tesoura x pedra   -> jogador 2
                "3, 3, 0"  // tesoura x tesoura -> empate
        })
        @DisplayName("As nove combinacoes possiveis")
        public void todasAsCombinacoes(int jogador1, int jogador2, int esperado) {
            assertEquals(esperado, jogo.jogar(jogador1, jogador2));
        }

        @ParameterizedTest(name = "jogar({0}, {1}) e jogar({1}, {0}) trocam o vencedor")
        @CsvSource({ "1, 2", "1, 3", "2, 3" })
        @DisplayName("Inverter as escolhas inverte o vencedor")
        public void resultadoEhSimetrico(int jogador1, int jogador2) {
            int direto = jogo.jogar(jogador1, jogador2);
            int invertido = jogo.jogar(jogador2, jogador1);
            int esperadoInvertido = (direto == VENCEU_JOGADOR_1) ? VENCEU_JOGADOR_2 : VENCEU_JOGADOR_1;

            Assertions.assertAll(
                    () -> Assertions.assertNotEquals(EMPATE, direto),
                    () -> assertEquals(esperadoInvertido, invertido));
        }
    }

    @Nested
    @DisplayName("Entradas invalidas")
    class EntradasInvalidas {

        @ParameterizedTest(name = "jogar({0}, 1) deve retornar -1")
        @ValueSource(ints = { 0, 4, -1, -10, 100, Integer.MIN_VALUE, Integer.MAX_VALUE })
        @DisplayName("Escolha invalida do jogador 1")
        public void jogador1Invalido(int escolhaInvalida) {
            assertEquals(INVALIDO, jogo.jogar(escolhaInvalida, PAPEL));
        }

        @ParameterizedTest(name = "jogar(1, {0}) deve retornar -1")
        @ValueSource(ints = { 0, 4, -1, -10, 100, Integer.MIN_VALUE, Integer.MAX_VALUE })
        @DisplayName("Escolha invalida do jogador 2")
        public void jogador2Invalido(int escolhaInvalida) {
            assertEquals(INVALIDO, jogo.jogar(PAPEL, escolhaInvalida));
        }

        @Test
        @DisplayName("Ambos os jogadores com escolha invalida")
        public void ambosInvalidos() {
            Assertions.assertAll(
                    () -> assertEquals(INVALIDO, jogo.jogar(0, 0)),
                    () -> assertEquals(INVALIDO, jogo.jogar(4, 4)),
                    () -> assertEquals(INVALIDO, jogo.jogar(-1, 9)));
        }

        @Test
        @DisplayName("Escolhas invalidas iguais nao contam como empate")
        public void invalidoIgualNaoEmpata() {
            assertEquals(INVALIDO, jogo.jogar(5, 5));
        }
    }

    @Nested
    @DisplayName("Limites do intervalo valido")
    class Limites {

        @ParameterizedTest(name = "jogar({0}, {1}) nao pode retornar -1")
        @CsvSource({ "1, 1", "1, 3", "3, 1", "3, 3" })
        @DisplayName("Os extremos 1 e 3 sao aceitos")
        public void extremosSaoValidos(int jogador1, int jogador2) {
            Assertions.assertNotEquals(INVALIDO, jogo.jogar(jogador1, jogador2));
        }

        @ParameterizedTest(name = "jogar({0}, {1}) deve retornar -1")
        @CsvSource({ "0, 1", "4, 1", "1, 0", "1, 4" })
        @DisplayName("Os vizinhos 0 e 4 sao rejeitados")
        public void vizinhosDosExtremosSaoInvalidos(int jogador1, int jogador2) {
            assertEquals(INVALIDO, jogo.jogar(jogador1, jogador2));
        }
    }
}
