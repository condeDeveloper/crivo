package br.com.conde.crivo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/** Os números que aparecem escritos no README, travados em teste. */
class MedidasTest {

    /* ===================== 1. correcao ===================== */

    @Test
    @DisplayName("zero divergencias contra a divisao, nas quatro versoes")
    void correcao() {
        for (Medidas.Correcao linha : Medidas.correcao())
            assertEquals(0, linha.divergencias(), "ate " + linha.ate());

        assertEquals(25, Medidas.correcao(100).primos());
        assertEquals(168, Medidas.correcao(1000).primos());
        assertEquals(9592, Medidas.correcao(100_000).primos());
    }

    /* ===================== 2. quadrado ===================== */

    @Test
    @DisplayName("COMECAR NO QUADRADO derruba as visitas de 10 milhoes para 3.161")
    void quadradoDerrubaVisitas() {
        Medidas.Custo ingenuo = Medidas.custo(10_000_000, "ingenuo");
        Medidas.Custo quadrado = Medidas.custo(10_000_000, "do quadrado");

        assertEquals(9_999_999L, ingenuo.visitas());
        assertEquals(3_161L, quadrado.visitas());

        /* E risca 22,45% a menos tambem: a mudanca mexe nas duas colunas. */
        assertEquals(29_465_738L, ingenuo.riscos());
        assertEquals(22_850_051L, quadrado.riscos());
    }

    @Test
    @DisplayName("pular os pares e o maior corte de riscos: 39%")
    void pularParesCorta() {
        Medidas.Ganho pares = Medidas.ganhos(10_000_000).get(1);

        assertEquals("do quadrado", pares.de());
        assertEquals("sem pares", pares.para());

        assertEquals(0.3906, pares.cortouDosRiscos(), 0.001);
    }

    /* ===================== 3. roda ===================== */

    @Test
    @DisplayName("A RODA DE 2 E 3 RISCA EXATAMENTE A MESMA COISA que pular pares")
    void rodaNaoRiscaMenos() {
        for (int ate : new int[] {100_000, 1_000_000, 10_000_000}) {
            Medidas.Custo pares = Medidas.custo(ate, "sem pares");
            Medidas.Custo roda = Medidas.custo(ate, "roda de 2 e 3");

            assertEquals(pares.riscos(), roda.riscos(), "ate " + ate);
        }
    }

    @Test
    @DisplayName("e o ganho dela inteiro sao 527 visitas em 13,9 milhoes de operacoes")
    void rodaGanhaQuaseNada() {
        Medidas.Custo pares = Medidas.custo(10_000_000, "sem pares");
        Medidas.Custo roda = Medidas.custo(10_000_000, "roda de 2 e 3");

        assertEquals(1580L, pares.visitas());
        assertEquals(1053L, roda.visitas());

        assertEquals(527L, pares.visitas() - roda.visitas());

        double fracao = (pares.total() - roda.total()) / (double) pares.total();

        assertTrue(fracao < 0.0001, "cortou " + (100 * fracao) + "%");
    }

    @Test
    @DisplayName("os dois primeiros passos cortam de verdade, o terceiro nao")
    void doisPassosValemUmNao() {
        var ganhos = Medidas.ganhos(1_000_000);

        assertTrue(ganhos.get(0).cortouDoTotal() > 0.40, "quadrado");
        assertTrue(ganhos.get(1).cortouDoTotal() > 0.35, "sem pares");
        assertTrue(ganhos.get(2).cortouDoTotal() < 0.001, "roda");
    }

    /* ===================== 4. divisao ===================== */

    @Test
    @DisplayName("a vantagem do crivo CRESCE: 5 vezes com dez mil, 26 com um milhao")
    void vantagemCresce() {
        assertEquals(5, Math.round(Medidas.divisao(10_000).vezes()));
        assertEquals(26, Math.round(Medidas.divisao(1_000_000).vezes()));

        double anterior = 0;

        for (Medidas.Comparacao linha : Medidas.divisao()) {
            assertTrue(linha.vezes() > anterior, "ate " + linha.ate());

            anterior = linha.vezes();
        }
    }

    /* ===================== 5. memoria ===================== */

    @Test
    @DisplayName("os contadores de primos conhecidos: 25, 168, 1.229, 9.592, 78.498")
    void contagensConhecidas() {
        assertEquals(25, Medidas.memoria(100).primos());
        assertEquals(168, Medidas.memoria(1_000).primos());
        assertEquals(1229, Medidas.memoria(10_000).primos());
        assertEquals(9592, Medidas.memoria(100_000).primos());
        assertEquals(78_498, Medidas.memoria(1_000_000).primos());
    }

    @Test
    @DisplayName("n/ln(n) SUBESTIMA sempre, e ainda erra 7,8% com um milhao")
    void estimativaSubestima() {
        for (Medidas.Densidade linha : Medidas.memoria()) {
            if (linha.ate() < 100) continue;

            assertTrue(linha.erroPorCento() < 0, "ate " + linha.ate() + " superestimou");
        }

        assertEquals(-7.8, Medidas.memoria(1_000_000).erroPorCento(), 0.1);

        /* E o erro encolhe conforme n cresce, mas devagar. */
        assertTrue(Math.abs(Medidas.memoria(1_000_000).erroPorCento())
                < Math.abs(Medidas.memoria(1_000).erroPorCento()));
    }

    @Test
    @DisplayName("o vetor de marcas e um byte por numero")
    void tamanhoDoVetor() {
        assertEquals(1_000_001L, Medidas.bytesDoVetor(1_000_000));
        assertEquals(10_000_001L, Medidas.bytesDoVetor(10_000_000));
    }
}
