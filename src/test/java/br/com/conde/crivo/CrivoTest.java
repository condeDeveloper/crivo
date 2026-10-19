package br.com.conde.crivo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * As quatro versões, e a divisão por tentativa que as julga.
 *
 * <p>O risco de um crivo é começar o laço um índice adiante e perder um primo sem que nada
 * acuse: o resultado continua sendo uma lista de números plausível. Por isso o juiz é a
 * divisão por tentativa, que não tem a forma de um crivo.
 */
class CrivoTest {

    private static final int[] PRIMEIROS = {2, 3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41,
        43, 47, 53, 59, 61, 67, 71, 73, 79, 83, 89, 97};

    private List<Integer> primosDe(boolean[] crivo) {
        List<Integer> saida = new ArrayList<>();

        for (int numero = 0; numero < crivo.length; numero++) if (crivo[numero]) saida.add(numero);

        return saida;
    }

    @Test
    @DisplayName("os primos ate 100 sao os 25 de sempre, nas quatro versoes")
    void primosAteCem() {
        List<Integer> esperados = new ArrayList<>();

        for (int primo : PRIMEIROS) esperados.add(primo);

        assertEquals(esperados, primosDe(Crivo.ingenuo(100)));
        assertEquals(esperados, primosDe(Crivo.doQuadrado(100)));
        assertEquals(esperados, primosDe(Crivo.semPares(100)));
        assertEquals(esperados, primosDe(Crivo.roda(100)));
    }

    @Test
    @DisplayName("0 e 1 nunca sao primos, e 2 sempre e")
    void casosDeBorda() {
        for (var versao : Medidas.VERSOES) {
            boolean[] crivo = versao.roda().apply(10);

            assertFalse(crivo[0], versao.nome());
            assertFalse(crivo[1], versao.nome());
            assertTrue(crivo[2], versao.nome());
            assertTrue(crivo[3], versao.nome());
            assertFalse(crivo[4], versao.nome());
        }
    }

    @Test
    @DisplayName("limites pequenos nao quebram nenhuma versao")
    void limitesPequenos() {
        for (var versao : Medidas.VERSOES)
            for (int ate = 0; ate <= 12; ate++) {
                boolean[] crivo = versao.roda().apply(ate);

                assertEquals(ate + 1, crivo.length, versao.nome() + " ate " + ate);

                for (int numero = 0; numero <= ate; numero++)
                    assertEquals(Crivo.ehPrimoPorDivisao(numero), crivo[numero],
                            versao.nome() + ": " + numero + " ate " + ate);
            }
    }

    @Test
    @DisplayName("AS QUATRO VERSOES DAO O MESMO VETOR, ate cem mil")
    void quatroVersoesIguais() {
        boolean[] referencia = Crivo.ingenuo(100_000);

        assertArrayEquals(referencia, Crivo.doQuadrado(100_000));
        assertArrayEquals(referencia, Crivo.semPares(100_000));
        assertArrayEquals(referencia, Crivo.roda(100_000));
    }

    @Test
    @DisplayName("e batem com a divisao por tentativa, numero a numero")
    void batemComADivisao() {
        boolean[] crivo = Crivo.roda(50_000);

        for (int numero = 0; numero <= 50_000; numero++)
            assertEquals(Crivo.ehPrimoPorDivisao(numero), crivo[numero], "numero " + numero);
    }

    @Test
    @DisplayName("a divisao por tentativa acerta os casos de cabeca")
    void divisaoAcerta() {
        for (int primo : PRIMEIROS) assertTrue(Crivo.ehPrimoPorDivisao(primo), "" + primo);

        for (int composto : new int[] {0, 1, 4, 6, 8, 9, 25, 49, 91, 99})
            assertFalse(Crivo.ehPrimoPorDivisao(composto), "" + composto);

        /* 91 = 7 x 13 e o classico que passa por primo em teste de cabeca. */
        assertFalse(Crivo.ehPrimoPorDivisao(91));
    }

    @Test
    @DisplayName("comecar no quadrado nao perde primo nenhum")
    void quadradoNaoPerde() {
        /* O medo ao comecar em p*p: perder multiplos pequenos. Eles ja foram riscados por
         * um fator menor, e isso vale conferir numero a numero. */
        boolean[] ingenuo = Crivo.ingenuo(20_000);
        boolean[] quadrado = Crivo.doQuadrado(20_000);

        for (int numero = 0; numero <= 20_000; numero++)
            assertEquals(ingenuo[numero], quadrado[numero], "numero " + numero);
    }
}
