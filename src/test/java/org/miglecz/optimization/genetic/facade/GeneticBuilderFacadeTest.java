package org.miglecz.optimization.genetic.facade;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.miglecz.optimization.Iteration.newIteration;
import static org.miglecz.optimization.Solution.newSolution;
import static org.miglecz.optimization.genetic.facade.GeneticBuilderFacade.builder;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import org.miglecz.optimization.Iteration;
import org.miglecz.optimization.genetic.Genetic;
import org.miglecz.optimization.genetic.TestBase;
import org.miglecz.optimization.genetic.facade.operator.Crossover;
import org.miglecz.optimization.genetic.facade.operator.Mutation;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class GeneticBuilderFacadeTest extends TestBase {
    @Test
    void buildShouldNotFailWithDefaultRandom() {
        // Given
        // When
        builder(Integer.class)
                //.withRandom(null)
                .withPopulation(0)
                .withFitness(impl -> 0)
                .withFactory(() -> 0)
                .build();
        // Then
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "random should not be null")
    void buildShouldFailWithRandomNull() {
        // Given
        // When
        builder(Integer.class)
                .withRandom(null)
                .build();
        // Then
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "population should not be null")
    void buildShouldFailWithDefaultPopulation() {
        // Given
        // When
        builder(Integer.class)
                //.withPopulation(null)
                .build();
        // Then
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "population should not be null")
    void buildShouldFailWithPopulationNull() {
        // Given
        // When
        builder(Integer.class)
                .withPopulation(null)
                .build();
        // Then
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "fitness should not be null")
    void buildShouldFailWithDefaultFitness() {
        // Given
        // When
        builder(Integer.class)
                .withPopulation(0)
                //.withFitness(null)
                .withFactory(() -> 0)
                .build();
        // Then
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "fitness should not be null")
    void buildShouldFailWithFitnessNull() {
        // Given
        // When
        builder(Integer.class)
                .withPopulation(0)
                .withFitness(null)
                .withFactory(() -> 0)
                .build();
        // Then
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "factory should not be null")
    void buildShouldFailWithDefaultFactory() {
        // Given
        // When
        builder(Integer.class)
                .withPopulation(0)
                .withFitness(impl -> 0)
                //.withFactory(null)
                .build();
        // Then
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "factory should not be null")
    void buildShouldFailWithFactoryNull() {
        // Given
        // When
        builder(Integer.class)
                .withPopulation(0)
                .withFitness(impl -> 0)
                .withFactory(null)
                .build();
        // Then
    }

    @DataProvider
    Object[][] data() {
        final Random random = new Random();
        return new Object[][]{
                new Object[]{List.of()}
                , new Object[]{List.of(1)}
                , new Object[]{List.of(1, 2)}
                , new Object[]{List.of(1, 2, 3)}
                , new Object[]{IntStream.range(0, random.nextInt(97) + 3).mapToObj(i -> random.nextInt(100)).collect(toUnmodifiableList())}
        };
    }

    @Test(dataProvider = "data")
    void streamShouldReturnFactoryGeneratedInitialSolutions(final List<Integer> impls) {
        // Given
        final AtomicInteger index = new AtomicInteger();
        final Genetic<Integer> genetic = builder(Integer.class)
                .withPopulation(impls.size())
                .withFitness(impl -> 0)
                .withFactory(() -> impls.get(index.getAndIncrement()))
                .build();
        // When
        final List<Iteration<Integer>> result = genetic.stream()
                .limit(1)
                .collect(toList());
        // Then
        assertThat(result, equalTo(List.of(
                newIteration(0, impls.stream().map(impl -> newSolution(0, impl)).collect(toList()))
        )));
    }

    @DataProvider
    Object[][] immigrantData() {
        return new Object[][]{
                new Object[]{0, List.of(1, 2), 1, 3, List.of(
                        newIteration(0, List.of())
                        , newIteration(1, List.of())
                        , newIteration(2, List.of())
                )}
                , new Object[]{1, List.of(1, 2, 3), 1, 3, List.of( //@formatter:off
                        newIteration(0, List.of(newSolution(0, 1)))
                        , newIteration(1, List.of(newSolution(0, 2)))
                        , newIteration(2, List.of(newSolution(0, 3)))
                )} //@formatter:on
                , new Object[]{1, List.of(1, 2, 3, 4, 5), 2, 3, List.of( //@formatter:off
                        newIteration(0, List.of(newSolution(0, 1)))
                        , newIteration(1, List.of(newSolution(0, 2)))
                        , newIteration(2, List.of(newSolution(0, 4)))
                )} //@formatter:on
                , new Object[]{2, List.of(1, 2, 3, 4), 1, 3, List.of( //@formatter:off
                        newIteration(0, List.of(newSolution(0, 1), newSolution(0, 2)))
                        , newIteration(1, List.of(newSolution(0, 3)))
                        , newIteration(2, List.of(newSolution(0, 4)))
                )} //@formatter:on
        };
    }

    @Test(dataProvider = "immigrantData")
    void streamShouldReturnImmigrants(final Integer population, final List<Integer> impls, final Integer immigrant, final int generation, final List<Iteration<Integer>> expected) {
        // Given
        final AtomicInteger index = new AtomicInteger();
        final Genetic<Integer> genetic = builder(Integer.class)
                .withPopulation(population)
                .withFitness(impl -> 0)
                .withFactory(() -> impls.get(index.getAndIncrement()))
                .withImmigrant(immigrant)
                .build();
        // When
        final List<Iteration<Integer>> result = genetic.stream()
                .limit(generation)
                .collect(toList());
        // Then
        assertThat(result, equalTo(expected));
    }

    @DataProvider
    Object[][] eliteData() {
        return new Object[][]{
                new Object[]{0, List.of(), 1, 3, List.of(
                        newIteration(0, List.of())
                        , newIteration(1, List.of())
                        , newIteration(2, List.of())
                )}
                , new Object[]{1, List.of(1), 1, 3, List.of( //@formatter:off
                        newIteration(0, List.of(newSolution(0, 1)))
                        , newIteration(1, List.of(newSolution(0, 1)))
                        , newIteration(2, List.of(newSolution(0, 1)))
                )} //@formatter:on
                , new Object[]{1, List.of(1), 2, 3, List.of( //@formatter:off
                        newIteration(0, List.of(newSolution(0, 1)))
                        , newIteration(1, List.of(newSolution(0, 1)))
                        , newIteration(2, List.of(newSolution(0, 1)))
                )} //@formatter:on
                , new Object[]{2, List.of(1, 2), 2, 3, List.of( //@formatter:off
                        newIteration(0, List.of(newSolution(0, 1), newSolution(0, 2)))
                        , newIteration(1, List.of(newSolution(0, 1), newSolution(0, 2)))
                        , newIteration(2, List.of(newSolution(0, 1), newSolution(0, 2)))
                )} //@formatter:on
        };
    }

    @Test(dataProvider = "eliteData")
    void streamShouldReturnElites(final Integer population, final List<Integer> impls, final Integer elite, final int generation, final List<Iteration<Integer>> expected) {
        // Given
        final AtomicInteger index = new AtomicInteger();
        final Genetic<Integer> genetic = builder(Integer.class)
                .withPopulation(population)
                .withFitness(impl -> 0)
                .withFactory(() -> impls.get(index.getAndIncrement()))
                .withElite(elite)
                .build();
        // When
        final List<Iteration<Integer>> result = genetic.stream()
                .limit(generation)
                .collect(toList());
        // Then
        assertThat(result, equalTo(expected));
    }

    @Test
    void streamShouldReturnFixedIterationSizes() {
        // Given
        final AtomicInteger index = new AtomicInteger(1);
        final Genetic<Integer> genetic = builder(Integer.class)
                .withPopulation(1)
                .withFitness(impl -> impl)
                .withFactory(index::getAndIncrement)
                .withElite(999)
                .withImmigrant(1)
                .build();
        // When
        final List<Iteration<Integer>> result = genetic.stream()
                .limit(4)
                .collect(toList());
        // Then
        assertThat(result, equalTo(List.of(
                newIteration(0, List.of(newSolution(1, 1)))
                , newIteration(1, List.of(newSolution(2, 2)))
                , newIteration(2, List.of(newSolution(3, 3)))
                , newIteration(3, List.of(newSolution(4, 4)))
        )));
    }

    @DataProvider
    Object[][] mutantsData() {
        return new Object[][]{
                new Object[]{0, List.of(), 0, (Mutation<Integer>) impl -> impl, 1, List.of(newIteration(0, List.of()))}
                , new Object[]{0, List.of(), 1, (Mutation<Integer>) impl -> null, 1, List.of(newIteration(0, List.of()))}
                , new Object[]{2, List.of(1, 2), 2, (Mutation<Integer>) impl -> impl + 1, 3, List.of( //@formatter:off
                        newIteration(0, List.of(newSolution(0, 1), newSolution(1, 2)))
                        , newIteration(1, List.of(newSolution(2, 3), newSolution(1, 2)))
                        , newIteration(2, List.of(newSolution(3, 4), newSolution(3, 4)))
                )} //@formatter:on
        };
    }

    @Test(dataProvider = "mutantsData")
    void streamShouldReturnMutants(
            final Integer population
            , final List<Integer> impls
            , final Integer mutant
            , final Mutation<Integer> mutation
            , final Integer limit
            , final List<Iteration<Integer>> expected
    ) {
        // Given
        final AtomicInteger index = new AtomicInteger(0);
        final Genetic<Integer> genetic = builder(Integer.class)
                .withPopulation(population)
                .withFitness(impl -> impl - 1)
                .withFactory(() -> impls.get(index.getAndIncrement()))
                .withRandom(new Random(1))
                .withMutant(mutant, mutation)
                .build();
        // When
        final List<Iteration<Integer>> result = genetic.stream()
                .limit(limit)
                .collect(toList());
        // Then
        assertThat(result, equalTo(expected));
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "mutant should not be null")
    void builderShouldFailWhenMutantNull() {
        // Given
        // When
        builder(Integer.class)
                .withPopulation(0)
                .withFitness(impl -> 0)
                .withFactory(() -> 0)
                .withMutant(null, i -> i)
                .build();
        // Then
    }

    @Test(expectedExceptions = NullPointerException.class, expectedExceptionsMessageRegExp = "mutation should not be null")
    void builderShouldFailWhenMutationNull() {
        // Given
        // When
        builder(Integer.class)
                .withPopulation(0)
                .withFitness(impl -> 0)
                .withFactory(() -> 0)
                .withMutant(0, null)
                .build();
        // Then
    }

    @DataProvider
    Object[][] offspringData() {
        return new Object[][]{
                new Object[]{0, List.of(), 0, (Crossover<Integer>) (a, b) -> null, 1, List.of(newIteration(0, List.of()))}
                , new Object[]{0, List.of(), 0, (Crossover<Integer>) (a, b) -> a * b, 2, List.of(newIteration(0, List.of()), newIteration(1, List.of()))}
                , new Object[]{2, List.of(2, 3), 20, (Crossover<Integer>) Integer::sum, 3, List.of( //@formatter:off
                        newIteration(0, List.of(newSolution(1, 2), newSolution(2, 3)))
                        , newIteration(1, List.of(newSolution(5, 6), newSolution(5, 6)))
                        , newIteration(2, List.of(newSolution(11, 12), newSolution(11, 12)))
                )} //@formatter:on
        };
    }

    @Test(dataProvider = "offspringData")
    void streamShouldReturnOffsprings(
            final Integer population
            , final List<Integer> initials
            , final Integer offspring
            , final Crossover<Integer> crossover
            , final Integer limit
            , final List<Iteration<Integer>> expected
    ) {
        // Given
        final AtomicInteger index = new AtomicInteger(0);
        final Genetic<Integer> genetic = builder(Integer.class)
                .withPopulation(population)
                .withFitness(impl -> impl - 1)
                .withFactory(() -> initials.get(index.getAndIncrement()))
                .withRandom(new Random(1))
                .withOffspring(offspring, crossover)
                .build();
        // When
        final List<Iteration<Integer>> result = genetic.stream()
                .limit(limit)
                .collect(toList());
        // Then
        assertThat(result, equalTo(expected));
    }
}
