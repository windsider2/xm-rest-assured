package com.api.automation.tests.film;

import com.api.automation.tests.BaseTest;
import io.restassured.response.Response;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.apache.http.HttpStatus.SC_OK;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class FilmTest extends BaseTest {
    private Response response;
    private String titleOfLatestRealiseFilm;
    private List<Map<String, Object>> filmsData;

    @BeforeClass
    public void getResponse() {
        response = getRequestSpecification()
                .get("/films")
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .extract().response();
        filmsData = response.jsonPath().getList("results");
    }

    @Test
    public void verifyFilmWithLatestRealiseDateTest() {
        //retrieving titles and realise dates from films' data
        List<Map<String, String>> realiseDates = filmsData.stream()
                .map(mp -> Map.of("title", mp.get("title").toString(), "releaseDate", mp.get("release_date").toString()))
                .collect(Collectors.toList());

        //looking for the film with the latest realise data
        Map<String, String> filmWithLatestRelease = realiseDates.stream()
                .max(Comparator.comparing(mp -> parseDate(mp.get("releaseDate"))))
                .orElseThrow(() -> new RuntimeException("No latest realise data found"));
        titleOfLatestRealiseFilm = filmWithLatestRelease.get("title");

        //verifying the film
        Map<String, String> expectedFilm = Map.of("releaseDate", "2005-05-19", "title", "Revenge of the Sith");
        assertThat(filmWithLatestRelease)
                .as("Wrong the latest realise date")
                .isEqualTo(expectedFilm);
    }

    @Test(dependsOnMethods = "verifyFilmWithLatestRealiseDateTest")
    public void verifyTheTallestCharacterFromFilmWithLatestRealiseDateTest() {
        //retrieving characters' urls from the film with the latest realise
        List<String> charactersUrls = filmsData.stream()
                .filter(mp -> titleOfLatestRealiseFilm.equals(mp.get("title")))
                .map(mp -> mp.get("characters"))
                .filter(List.class::isInstance)
                .flatMap(charList -> ((List<String>) charList).stream())
                .collect(Collectors.toList());

        //finding the tallest person among the characters that were part of that film.
        Map<String, String> tallestCharacter = findTheTallestCharacter(charactersUrls);

        //verifying the tallest character
        Map<String, String> expectedTallestCharacter = Map.of("Tarfful", "234");
        assertThat(tallestCharacter)
                .as("Wrong the tallest character")
                .isEqualTo(expectedTallestCharacter);
    }

    @Test
    public void tallestCharacterTest() {
        //retrieving characters' urls from the all films
        List<String> charactersUrls = filmsData.stream()
                .map(mp -> mp.get("characters"))
                .filter(List.class::isInstance)
                .flatMap(charList -> ((List<String>) charList).stream())
                .collect(Collectors.toList());

        //finding the tallest person among the characters any Star Wars film.
        Map<String, String> tallestCharacter = findTheTallestCharacter(charactersUrls);

        //verifying the tallest character
        Map<String, String> expectedTallestCharacter = Map.of("Yarael Poof", "264");
        assertThat(tallestCharacter)
                .as("Wrong the tallest character")
                .isEqualTo(expectedTallestCharacter);


    }

    @Test
    public void verifyPeopleSchemaTest() {
        getRequestSpecification()
                .get("/people")
                .then()
                .assertThat()
                .statusCode(SC_OK)
                .body(matchesJsonSchemaInClasspath("people-schema.json"));
    }


    private Map<String, String> findTheTallestCharacter(List<String> urls) {
        return urls.stream()
                .map(url -> getRequestSpecification().get(url).then().statusCode(200).extract().jsonPath())
                .filter(jsonPath -> jsonPath.getString("height").matches("^\\d{2,}$"))
                .max(Comparator.comparingInt(jsonPath -> Integer.parseInt(jsonPath.getString("height"))))
                .map(jsonPath -> Map.of(jsonPath.getString("name"), jsonPath.getString("height")))
                .orElseThrow(() -> new RuntimeException("No characters found"));

    }

    private LocalDate parseDate(String date) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(date, formatter);
    }
}

