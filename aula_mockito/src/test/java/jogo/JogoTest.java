package jogo;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class JogoTest {

	private final Jogador jogadorMock = mock(Jogador.class);
    private final Dado dadinho1 = new Dado();
	private final Dado dadinho2 = new Dado();

    private Jogo jogo = new Jogo();

    @Test
    @DisplayName("Primeiro turno: ganha se 7")
    public void ganhaPrimeiroTurno() {
        when(jogadorMock.lancar(dadinho1, dadinho2)).thenReturn(7);
        assertTrue(jogo.jogo(jogadorMock, dadinho1, dadinho2), "com soma 7 no primeiro turno o jogador deveria ganhar");
    }

    @Test
    @DisplayName("Primeiro turno: ganha se 11")
    public void ganhaPrimeiroTurno11() {
        when(jogadorMock.lancar(dadinho1, dadinho2)).thenReturn(11);
        assertTrue(jogo.jogo(jogadorMock, dadinho1, dadinho2));
    }

    @Test
    @DisplayName("Primeiro turno: perde se 2")
    public void perdePrimeiroTurno2() {
        when(jogadorMock.lancar(dadinho1, dadinho2)).thenReturn(2);
        assertFalse(jogo.jogo(jogadorMock, dadinho1, dadinho2));
    }

    @Test
    @DisplayName("Primeiro turno: perde se 3")
    public void perdePrimeiroTurno3() {
        when(jogadorMock.lancar(dadinho1, dadinho2)).thenReturn(3);
        assertFalse(jogo.jogo(jogadorMock, dadinho1, dadinho2));
    }

    @Test
    @DisplayName("Primeiro turno: perde se 12")
    public void perdePrimeiroTurno12() {
        when(jogadorMock.lancar(dadinho1, dadinho2)).thenReturn(12);
        assertFalse(jogo.jogo(jogadorMock, dadinho1, dadinho2));
    }

    @Test
    @DisplayName("Segundo turno: ganha se repetir o ponto")
    public void ganhaSegundoTurno() {
        when(jogadorMock.lancar(dadinho1, dadinho2)).thenReturn(5, 5);
        assertTrue(jogo.jogo(jogadorMock, dadinho1, dadinho2));
    }

    @Test
    @DisplayName("Segundo turno: perde se tirar 7")
    public void perdeSegundoTurno() {
        when(jogadorMock.lancar(dadinho1, dadinho2)).thenReturn(5, 7);
        assertFalse(jogo.jogo(jogadorMock, dadinho1, dadinho2));
    }
}