package calculadora;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class CalculadoraTest {
	
	// @BeforeEach
	private Calculadora calc = new Calculadora();
	
	@Test
	public void testSomaDoisNumeros() {
		int soma = calc.soma(3, 4);
		assertEquals(7, soma);
	}
	
	@Test
	public void testSubtracaoDoisNumeros() {
		int subtracao = calc.subtracao(4,3);
		assertEquals(1, subtracao);
	}

	@Test
	public void testMultiplicacaoDoisNumreos() {
		int multiplica = calc.multiplicacao(7,3);
		assertEquals(21, multiplica);
	}

	@Test
	public void testDivisaoDoisNumeros() {
		int divisao = calc.divisao(10, 2);
		assertTrue(divisao == 5);
	}

}
