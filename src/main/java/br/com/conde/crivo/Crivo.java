package br.com.conde.crivo;

/**
 * O CRIVO DE ERATÓSTENES, de uns 240 antes de Cristo, em quatro versões.
 *
 * <p>A ideia é a mesma nas quatro: escreva os números, pegue o primeiro que sobrou, risque
 * todos os múltiplos dele, repita. O que sobra são os primos.
 *
 * <p>As quatro versões existem porque o crivo é o exemplo clássico de algoritmo onde as
 * "otimizações óbvias" valem coisas muito diferentes, e quase ninguém mede qual vale o quê.
 * Elas estão aqui separadas para que a diferença seja um número e não uma impressão.
 *
 * <p>O contador de {@link #riscos()} não serve ao algoritmo: ele conta quantas vezes uma
 * posição foi marcada, que é o trabalho de verdade.
 */
public final class Crivo {

    private Crivo() {}

    /** Quantas marcações foram feitas, contando repetição. */
    public static long riscos;

    /** Quantas posições do vetor foram tocadas para decidir se riscar. */
    public static long visitas;

    public static void zerar() {
        riscos = 0;
        visitas = 0;
    }

    /**
     * O INGÊNUO: para cada primo, risca a partir do DOBRO dele.
     *
     * <p>É a tradução literal de "risque os múltiplos", e ela risca muita coisa duas vezes: o
     * 6 é riscado pelo 2 e de novo pelo 3, o 12 é riscado pelo 2, pelo 3, e já estava riscado.
     */
    public static boolean[] ingenuo(int ate) {
        boolean[] composto = new boolean[ate + 1];

        for (int numero = 2; numero <= ate; numero++) {
            visitas++;

            if (composto[numero]) continue;

            for (long multiplo = 2L * numero; multiplo <= ate; multiplo += numero) {
                riscos++;

                composto[(int) multiplo] = true;
            }
        }

        return naoCompostos(composto, ate);
    }

    /**
     * COMEÇANDO NO QUADRADO: para o primo p, risca a partir de p vezes p.
     *
     * <p>O motivo: todo múltiplo de p menor que p² tem um fator menor que p, então já foi
     * riscado por ele. Riscar de novo não é errado, é desperdício.
     *
     * <p>E o laço de fora pode parar na raiz, pelo mesmo motivo: se um número tem fator maior
     * que a raiz, ele tem outro menor, e esse já passou.
     */
    public static boolean[] doQuadrado(int ate) {
        boolean[] composto = new boolean[ate + 1];

        for (int numero = 2; (long) numero * numero <= ate; numero++) {
            visitas++;

            if (composto[numero]) continue;

            for (long multiplo = (long) numero * numero; multiplo <= ate; multiplo += numero) {
                riscos++;

                composto[(int) multiplo] = true;
            }
        }

        return naoCompostos(composto, ate);
    }

    /**
     * PULANDO OS PARES: trata o 2 à parte e só olha ímpares.
     *
     * <p>Metade dos números some do problema de uma vez, e o passo do risco vira o dobro do
     * primo, porque somar o primo a um ímpar dá um par, que não interessa.
     */
    public static boolean[] semPares(int ate) {
        boolean[] composto = new boolean[ate + 1];

        if (ate >= 4)
            for (long multiplo = 4; multiplo <= ate; multiplo += 2) {
                riscos++;

                composto[(int) multiplo] = true;
            }

        for (int numero = 3; (long) numero * numero <= ate; numero += 2) {
            visitas++;

            if (composto[numero]) continue;

            /* Passo de dois primos: pular os multiplos pares, que ja foram. */
            for (long multiplo = (long) numero * numero; multiplo <= ate;
                    multiplo += 2L * numero) {
                riscos++;

                composto[(int) multiplo] = true;
            }
        }

        return naoCompostos(composto, ate);
    }

    /**
     * A RODA DE 2 E 3: só olha números das formas {@code 6k-1} e {@code 6k+1}.
     *
     * <p>Todo primo maior que 3 é de uma dessas duas formas, porque as outras quatro são
     * divisíveis por 2 ou por 3. Isso tira dois terços dos números do problema, contra metade
     * de pular só os pares.
     *
     * <p>A quarta medida mostra que o segundo terço custa bem menos que o primeiro: a roda é
     * mais complicada e ganha menos do que a troca anterior.
     */
    public static boolean[] roda(int ate) {
        boolean[] composto = new boolean[ate + 1];

        if (ate >= 4)
            for (long multiplo = 4; multiplo <= ate; multiplo += 2) {
                riscos++;

                composto[(int) multiplo] = true;
            }

        if (ate >= 9)
            for (long multiplo = 9; multiplo <= ate; multiplo += 6) {
                riscos++;

                composto[(int) multiplo] = true;
            }

        /* Os candidatos: 5, 7, 11, 13, 17, 19, ... alternando os saltos de 2 e 4. */
        int salto = 2;

        for (int numero = 5; (long) numero * numero <= ate; numero += salto, salto = 6 - salto) {
            visitas++;

            if (composto[numero]) continue;

            for (long multiplo = (long) numero * numero; multiplo <= ate;
                    multiplo += 2L * numero) {
                riscos++;

                composto[(int) multiplo] = true;
            }
        }

        return naoCompostos(composto, ate);
    }

    private static boolean[] naoCompostos(boolean[] composto, int ate) {
        boolean[] primo = new boolean[ate + 1];

        for (int numero = 2; numero <= ate; numero++) primo[numero] = !composto[numero];

        return primo;
    }

    /**
     * A DIVISÃO POR TENTATIVA, que é o jeito de quem não conhece o crivo.
     *
     * <p>Para cada número, tenta dividir por todos os candidatos até a raiz. Ela responde a
     * outra pergunta, "este aqui é primo", e serve de juiz por não ter nada em comum com
     * riscar múltiplos.
     */
    public static boolean ehPrimoPorDivisao(int numero) {
        if (numero < 2) return false;

        if (numero < 4) return true;

        if (numero % 2 == 0) return false;

        for (int divisor = 3; (long) divisor * divisor <= numero; divisor += 2) {
            visitas++;

            if (numero % divisor == 0) return false;
        }

        return true;
    }

    public static int quantosPrimos(boolean[] primo) {
        int quantos = 0;

        for (boolean ehPrimo : primo) if (ehPrimo) quantos++;

        return quantos;
    }
}
