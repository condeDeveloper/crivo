package br.com.conde.crivo;

import java.util.ArrayList;
import java.util.List;

/**
 * AS MEDIDAS: as quatro versões do crivo contadas em riscos e em visitas, e o crivo contra a
 * divisão por tentativa.
 *
 * <p>A separação entre RISCO e VISITA é o que faz este repositório ter algo a dizer. Risco é
 * marcar uma posição como composta. Visita é olhar um candidato para decidir se ele vale a
 * pena. Quase toda discussão sobre otimizar crivo mistura as duas, e uma das quatro versões
 * melhora só a segunda.
 */
public final class Medidas {

    private Medidas() {}

    public record Versao(String nome, java.util.function.IntFunction<boolean[]> roda) {}

    public static final List<Versao> VERSOES = List.of(
        new Versao("ingenuo", Crivo::ingenuo),
        new Versao("do quadrado", Crivo::doQuadrado),
        new Versao("sem pares", Crivo::semPares),
        new Versao("roda de 2 e 3", Crivo::roda));

    /* ===================== 1. correcao ===================== */

    public record Correcao(int ate, int primos, int divergencias) {}

    private static List<Correcao> correcao;

    /**
     * AS QUATRO VERSÕES, CONTRA A DIVISÃO POR TENTATIVA.
     *
     * <p>As quatro riscam múltiplos; a divisão por tentativa pergunta, para cada número, se
     * algum candidato até a raiz o divide. São ideias diferentes, e por isso a divisão serve
     * de juiz: ela não pode errar do mesmo jeito que um crivo com índice trocado erraria.
     */
    public static List<Correcao> correcao() {
        if (correcao != null) return correcao;

        List<Correcao> saida = new ArrayList<>();

        for (int ate : new int[] {100, 1_000, 10_000, 100_000}) {
            boolean[][] resultados = new boolean[VERSOES.size()][];

            for (int indice = 0; indice < VERSOES.size(); indice++)
                resultados[indice] = VERSOES.get(indice).roda().apply(ate);

            int divergencias = 0;

            for (int numero = 0; numero <= ate; numero++) {
                boolean porDivisao = Crivo.ehPrimoPorDivisao(numero);

                for (boolean[] resultado : resultados)
                    if (resultado[numero] != porDivisao) divergencias++;
            }

            saida.add(new Correcao(ate, Crivo.quantosPrimos(resultados[0]), divergencias));
        }

        return correcao = List.copyOf(saida);
    }

    public static Correcao correcao(int ate) {
        return correcao().stream().filter(x -> x.ate() == ate).findFirst().orElseThrow();
    }

    /* ===================== 2, 3: o custo de cada versao ===================== */

    public record Custo(int ate, String versao, long riscos, long visitas) {

        public long total() {
            return riscos + visitas;
        }
    }

    private static List<Custo> custo;

    /**
     * QUANTO CADA VERSÃO TRABALHA.
     *
     * <p>Duas colunas de propósito. Começar no quadrado mexe nas duas: risca menos E, por
     * parar o laço de fora na raiz, visita muito menos. Pular os pares mexe só nos riscos. A
     * roda mexe só nas visitas, e as visitas já eram quase nada.
     */
    public static List<Custo> custo() {
        if (custo != null) return custo;

        List<Custo> saida = new ArrayList<>();

        for (int ate : new int[] {100_000, 1_000_000, 10_000_000})
            for (Versao versao : VERSOES) {
                Crivo.zerar();

                versao.roda().apply(ate);

                saida.add(new Custo(ate, versao.nome(), Crivo.riscos, Crivo.visitas));
            }

        return custo = List.copyOf(saida);
    }

    public static Custo custo(int ate, String versao) {
        return custo().stream().filter(x -> x.ate() == ate && x.versao().equals(versao))
                .findFirst().orElseThrow();
    }

    /** O quanto cada passo cortou do anterior, em riscos e no total. */
    public record Ganho(int ate, String de, String para, double cortouDosRiscos,
            double cortouDoTotal) {}

    public static List<Ganho> ganhos(int ate) {
        List<Ganho> saida = new ArrayList<>();

        for (int indice = 1; indice < VERSOES.size(); indice++) {
            Custo antes = custo(ate, VERSOES.get(indice - 1).nome());
            Custo depois = custo(ate, VERSOES.get(indice).nome());

            saida.add(new Ganho(ate, antes.versao(), depois.versao(),
                    (antes.riscos() - depois.riscos()) / (double) antes.riscos(),
                    (antes.total() - depois.total()) / (double) antes.total()));
        }

        return saida;
    }

    /* ===================== 4. divisao ===================== */

    public record Comparacao(int ate, long crivo, long divisao) {

        public double vezes() {
            return crivo == 0 ? 0 : divisao / (double) crivo;
        }
    }

    private static List<Comparacao> divisao;

    /**
     * O CRIVO CONTRA A DIVISÃO POR TENTATIVA.
     *
     * <p>A divisão responde "este é primo" e o crivo responde "quais são os primos". Para a
     * segunda pergunta, o crivo ganha por margem que cresce, e é esse crescimento que importa:
     * não é uma constante melhor, é outra ordem de trabalho.
     */
    public static List<Comparacao> divisao() {
        if (divisao != null) return divisao;

        List<Comparacao> saida = new ArrayList<>();

        for (int ate : new int[] {10_000, 100_000, 1_000_000}) {
            Crivo.zerar();

            Crivo.roda(ate);

            long doCrivo = Crivo.riscos + Crivo.visitas;

            Crivo.zerar();

            for (int numero = 2; numero <= ate; numero++) Crivo.ehPrimoPorDivisao(numero);

            saida.add(new Comparacao(ate, doCrivo, Crivo.visitas));
        }

        return divisao = List.copyOf(saida);
    }

    public static Comparacao divisao(int ate) {
        return divisao().stream().filter(x -> x.ate() == ate).findFirst().orElseThrow();
    }

    /* ===================== 5. memoria ===================== */

    public record Densidade(int ate, int primos, double estimativa, double erroPorCento) {}

    private static List<Densidade> memoria;

    /**
     * QUANTOS PRIMOS EXISTEM, e o que a estimativa clássica erra.
     *
     * <p>{@code n / ln(n)} é a primeira aproximação do teorema dos números primos, e ela
     * subestima sempre, com erro caindo devagar. A tabela mostra o erro ainda em 7,8% com um
     * milhão, que é bem mais do que a fama da fórmula sugere.
     */
    public static List<Densidade> memoria() {
        if (memoria != null) return memoria;

        List<Densidade> saida = new ArrayList<>();

        for (int ate : new int[] {100, 1_000, 10_000, 100_000, 1_000_000}) {
            int primos = Crivo.quantosPrimos(Crivo.roda(ate));

            double estimativa = ate / Math.log(ate);

            saida.add(new Densidade(ate, primos, estimativa,
                    100 * (estimativa - primos) / primos));
        }

        return memoria = List.copyOf(saida);
    }

    public static Densidade memoria(int ate) {
        return memoria().stream().filter(x -> x.ate() == ate).findFirst().orElseThrow();
    }

    /** Quantos bytes o vetor de marcas ocupa, que é o que limita o crivo na prática. */
    public static long bytesDoVetor(int ate) {
        return ate + 1L;
    }
}
