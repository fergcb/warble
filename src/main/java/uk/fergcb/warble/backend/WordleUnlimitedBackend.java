package uk.fergcb.warble.backend;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import uk.fergcb.warble.WordEvaluation;
import uk.fergcb.warble.scoring.LetterEvaluation;

import java.util.ArrayList;
import java.util.stream.Collectors;

public class WordleUnlimitedBackend implements WordleBackend {

    private final WebDriver driver;

    public WordleUnlimitedBackend() {
        System.setProperty("webdriver.chrome.driver", "src/main/resources/chromedriver.exe");

        final var options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        final var uBlockDir = System.getProperty("user.dir") + "/ublock";
        options.addArguments("load-extension=" + uBlockDir);

        this.driver = new ChromeDriver(options);

        driver.get("https://wordleunlimited.org/");
        sleep(10000);
        new Actions(driver).click().perform();
    }

    @Override
    public void submitGuess(String word) {
        new Actions(driver)
                .sendKeys(word)
                .sendKeys(Keys.ENTER)
                .perform();
        sleep(3000);
    }

    @Override
    public boolean isFinished() {
        final var modal = driver.findElement(By.cssSelector("game-app")).getShadowRoot()
                .findElement(By.cssSelector("game-theme-manager"))
                .findElement(By.cssSelector("game-modal"));
        return modal.getDomAttribute("open") != null;
    }

    @Override
    public void close() {
        if (driver != null) {
            driver.close();
            driver.quit();
        }
    }

    @Override
    public WordEvaluation evaluateWord(int guess) {
        final var tiles = driver.findElement(By.cssSelector("game-app")).getShadowRoot()
                .findElement(By.cssSelector("game-theme-manager"))
                .findElements(By.cssSelector("game-row")).get(guess).getShadowRoot()
                .findElements(By.cssSelector("game-tile"));

        System.out.println(" -> " + tiles.stream()
                .map(tile -> switch (tile.getDomAttribute("evaluation")) {
                    case "correct" -> "\uD83D\uDFE9";
                    case "present" -> "\uD83D\uDFE8";
                    default -> "⬛";
                })
                .collect(Collectors.joining()));

        final var letterEvals = new ArrayList<LetterEvaluation>();

        for (int i = 0; i < tiles.size(); i++) {
            final var tile = tiles.get(i);
            final var letter = tile.getDomAttribute("letter").toUpperCase().charAt(0);
            letterEvals.add(switch (tile.getDomAttribute("evaluation")) {
                case "correct" -> LetterEvaluation.correct(letter, i);
                case "present" -> LetterEvaluation.present(letter, i);
                default -> LetterEvaluation.absent(letter, i);
            });
        }

        return new WordEvaluation(letterEvals);
    }

    private void sleep(long duration) {
        try { Thread.sleep(duration); }
        catch (Exception ignored) { }
    }
}
