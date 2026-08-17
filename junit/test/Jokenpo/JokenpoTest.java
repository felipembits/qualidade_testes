package jokenpo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class JokenpoTest {

    private Main jogo = new Main();

    @Test
    public void testEmpatePedra() {
        int empatePedra = jogo.jogar(2,2);
        assertEquals(empatePedra, 0);
    }

    @Test
    public void testEmpatePapel() {
        int empatePapel = jogo.jogar(1,1);
        assertEquals(empatePapel, 0);
    }

    @Test
    public void testEmpateTesoura() {
        int empateTesoura = jogo.jogar(3,3);
        assertEquals(empateTesoura, 0);
    }

    @Test
    public void testPapelPedra() {
        int papelPedra = jogo.jogar(1,2);
        assertEquals(papelPedra, 1);
    }

    @Test
    public void testPapelTesoura() {
        int papelTesoura = jogo.jogar(1,3);
        assertEquals(papelTesoura, 2);
    }

    @Test
    public void testPedraTesoura() {
        int pedraTesoura = jogo.jogar(2,3);
        assertEquals(pedraTesoura, 1);
    }
}
