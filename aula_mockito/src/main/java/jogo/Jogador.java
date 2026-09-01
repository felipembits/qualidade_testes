package jogo;

public class Jogador {
	
	public int lancar(Dado dado1, Dado dado2) {
		int resultado = dado1.numero() + dado2.numero();
		return resultado;
	}
	
}
