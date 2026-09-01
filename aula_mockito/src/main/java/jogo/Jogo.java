package jogo;

public class Jogo {

	
	/*
	 * Se for o primeiro turno e a soma das faces dos dados cair 7 ou 11 você ganha o jogo;
	 * Se for o primeiro turno e a soma das faces dos dados cair 2, 3 e 12 você perde o jogo; 
	 * Se não cair nenhum desses valores, o valor é armazenado e passa para o segundo turno;
	 * Se for o segundo turno e a soma das faces dos dados cair 7 novamente, você perde o jogo;
	 * Se for o segundo turno, você continuando jogando os dados e só ganha se cair um numero igual ao anterior do primeiro turno;
	 */
	public boolean jogo(Jogador jogador, Dado dadinho1, Dado dadinho2){
		
		int resultado = jogador.lancar(dadinho1, dadinho2);
		
		if(resultado == 7 || resultado == 11) {
			return true;
		}else if(resultado == 2 || resultado == 3 || resultado == 12){
			return false;
		}
		
		int ponto = resultado;
		
		resultado = jogador.lancar(dadinho1, dadinho2);
	
		while(resultado != ponto && resultado != 7){
			resultado = jogador.lancar(dadinho1, dadinho2);
		}
		
		return resultado == ponto;
		
	}
}
