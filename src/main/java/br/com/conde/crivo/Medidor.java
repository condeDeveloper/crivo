package br.com.conde.crivo;

import java.util.Locale;

/** O MEDIDOR: roda as varreduras e imprime as tabelas do README. */
public final class Medidor {

    private Medidor() {}

    public static void main(String[] argumentos) {
        Locale.setDefault(Locale.US);

        String passo = argumentos.length > 0 ? argumentos[0] : "tudo";

        switch (passo) {
            case "correcao" -> correcao();
            case "quadrado" -> quadrado();
            case "roda" -> roda();
            case "divisao" -> divisao();
            case "memoria" -> memoria();
            default -> {
                correcao();
                quadrado();
                roda();
                divisao();
                memoria();
            }
        }
    }

    private static void titulo(String texto) {
        System.out.println();
        System.out.println(texto);
        System.out.println("=".repeat(texto.length()));
        System.out.println();
    }

    static void correcao() {
        titulo("1. as quatro versoes contra a divisao por tentativa");

        System.out.println("ate        primos  divergencias");

        for (Medidas.Correcao linha : Medidas.correcao())
            System.out.printf("%9d  %6d  %12d%n", linha.ate(), linha.primos(),
                    linha.divergencias());

        System.out.println();
        System.out.println("as quatro riscam multiplos; a divisao pergunta, para cada numero,");
        System.out.println("se algum candidato ate a raiz o divide. sao ideias diferentes, e");
        System.out.println("por isso a divisao pode julgar.");
    }

    static void quadrado() {
        titulo("2. quanto cada versao trabalha");

        System.out.println("ate         versao          riscos    visitas       total");

        for (Medidas.Custo linha : Medidas.custo())
            System.out.printf("%9d  %-14s %10d %10d %11d%n", linha.ate(), linha.versao(),
                    linha.riscos(), linha.visitas(), linha.total());

        System.out.println();
        System.out.println("duas colunas de proposito. RISCO e marcar uma posicao como");
        System.out.println("composta; VISITA e olhar um candidato para decidir se vale a pena.");
        System.out.println();
        System.out.println("comecar no quadrado mexe nas DUAS: risca menos e, por parar o laco");
        System.out.println("de fora na raiz, derruba as visitas de 10 milhoes para 3.161.");
    }

    static void roda() {
        titulo("3. o que cada otimizacao vale de verdade");

        for (int ate : new int[] {100_000, 1_000_000, 10_000_000}) {
            System.out.printf("ate %d:%n", ate);

            for (Medidas.Ganho g : Medidas.ganhos(ate))
                System.out.printf("  %-14s -> %-14s cortou %6.2f%% dos riscos, %6.2f%% do total%n",
                        g.de(), g.para(), 100 * g.cortouDosRiscos(), 100 * g.cortouDoTotal());

            System.out.println();
        }

        Medidas.Custo pares = Medidas.custo(10_000_000, "sem pares");
        Medidas.Custo roda = Medidas.custo(10_000_000, "roda de 2 e 3");

        System.out.println("A RODA DE 2 E 3 RISCA EXATAMENTE A MESMA COISA que pular pares:");
        System.out.printf("  sem pares: %d riscos, roda: %d riscos%n", pares.riscos(),
                roda.riscos());
        System.out.printf("  a diferenca inteira sao %d visitas em %d operacoes: %.4f%%%n",
                pares.visitas() - roda.visitas(), pares.total(),
                100.0 * (pares.visitas() - roda.visitas()) / pares.total());

        System.out.println();
        System.out.println("ela e mais complicada de escrever, mais facil de errar, e nao risca");
        System.out.println("um numero a menos. o ganho dela esta numa coluna que ja era zero.");
    }

    static void divisao() {
        titulo("4. o crivo contra a divisao por tentativa");

        System.out.println("ate         crivo       divisao   quantas vezes");

        for (Medidas.Comparacao linha : Medidas.divisao())
            System.out.printf("%9d  %10d  %12d  %13.0fx%n", linha.ate(), linha.crivo(),
                    linha.divisao(), linha.vezes());

        System.out.println();
        System.out.println("a margem CRESCE: 5 vezes com dez mil, 26 com um milhao. nao e uma");
        System.out.println("constante melhor, e outra ordem de trabalho.");
        System.out.println();
        System.out.println("mas vale dizer: as duas respondem perguntas diferentes. a divisao");
        System.out.println("diz se UM numero e primo sem precisar de memoria nenhuma.");
    }

    static void memoria() {
        titulo("5. quantos primos existem, e o que a estimativa erra");

        System.out.println("ate         primos    n/ln(n)    erro");

        for (Medidas.Densidade linha : Medidas.memoria())
            System.out.printf("%9d  %9d  %9.1f  %6.1f%%%n", linha.ate(), linha.primos(),
                    linha.estimativa(), linha.erroPorCento());

        System.out.println();
        System.out.printf("o vetor de marcas para um milhao ocupa %d bytes, e para dez milhoes,%n",
                Medidas.bytesDoVetor(1_000_000));
        System.out.printf("%d. e isso, e nao o tempo, que limita o crivo na pratica.%n",
                Medidas.bytesDoVetor(10_000_000));

        System.out.println();
        System.out.println("n dividido por ln(n) SUBESTIMA sempre, e o erro cai devagar: ainda");
        System.out.println("sao 7,8% com um milhao, bem mais do que a fama da formula sugere.");
    }
}
