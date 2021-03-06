package org.mamasdelrio.android.logic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mamasdelrio.android.BuildConfig;
import org.mamasdelrio.android.logic.MessageFormatter;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link MessageFormatter}.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class)
public class MessageFormatterTest {
  MessageFormatter formatter;

  @Before
  public void before() {
    formatter = new MessageFormatter();
  }

  @Test
  public void getFinalMessageCorrect() {
    String userFriendlyBody = "Hello everyone. We have a new baby.";
    String jsonBody = "{fake: 4}";
    String expected = jsonBody + formatter.getDelimiter() + userFriendlyBody;
    String actual = formatter.createFinalMessage(userFriendlyBody, jsonBody);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void interpolateMessageSimpleCase() throws Exception {
    // A dead simple example as given in the documentation.
    String rawMsg = "Hello ${name}. You are ${age} years old.";
    Map<String, String> map = new HashMap<>();
    map.put("name", "David");
    map.put("age", "95");

    String expected = "Hello David. You are 95 years old.";
    String actual = formatter.interpolateMessage(rawMsg, map);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void interpolateMessageKeyAbsent() {
    // We should ignore failing keys.
    String rawMsg = "My name is ${absent}";
    Map<String, String> map = new HashMap<>();

    String actual = formatter.interpolateMessage(rawMsg, map);

    // Since we aren't interpolating anything, this should leave the string
    // untouched.
    assertThat(actual).isEqualTo(rawMsg);
  }

  @Test
  public void interpolateMessageHandlesMany() {
    String rawMessage = "${a} and ${ab} and ${abc} and ${abcd} and ${abcde}";
    Map<String, String> map = new HashMap<>();
    map.put("a", "howdy");
    map.put("ab", "what? is up");
    map.put("abc", "Jeffery");
    map.put("abcd", "");
    map.put("abcde", "and then. a longer one");

    String expected =
        "howdy and what? is up and Jeffery and  and and then. a longer one";
    String actual = formatter.interpolateMessage(rawMessage, map);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void interpolateMessageDoesPartialInterpolation() {
    // We should interpolate the keys we have.
    String rawMsg = "${a} and ${b} are sitting in a ${c}";
    Map<String, String> map = new HashMap<>();
    map.put("b", "middle");

    String expected = "${a} and middle are sitting in a ${c}";
    String actual = formatter.interpolateMessage(rawMsg, map);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void interpolateMessageReplacesRepeats() {
    String rawMsg = "maps by the ${yes} ${yes} ${yes}s.";
    Map<String, String> map = new HashMap<>();
    map.put("yes", "Yeah");

    String expected = "maps by the Yeah Yeah Yeahs.";
    String actual = formatter.interpolateMessage(rawMsg, map);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void interpolateMessageHandlesRecursiveReplace() {
    // We want to make sure we don't get stuck if we input another match.
    String rawMsg = "my name is ${name}";
    Map<String, String> map = new HashMap<>();
    map.put("name", "${name}");

    String actual = formatter.interpolateMessage(rawMsg, map);

    // We should just assume that we have a check to not recurse infinitely
    assertThat(actual).isEqualTo(rawMsg);
  }

  @Test
  public void interpolateMessageHandlesTrickyChars() {
    // Try and create a complicated string.
    String rawMsg = "in${t}nal $${leave}} is $${cost} and ${${double}}";
    Map<String, String> map = new HashMap<>();
    map.put("t", "ter");
    map.put("leave", "foo");
    map.put("cost", "45");
    map.put("double", "woot");

    String expected = "internal $foo} is $45 and ${woot}";
    String actual = formatter.interpolateMessage(rawMsg, map);

    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void interpolateMessageReturnsRawOnSyntaxError() {
    // Note the intentional syntax error here.
    String illegalMsg = "Hello ${name}. You are {$age} years old";
    Map<String, String> map = new HashMap<>();
    map.put("name", "David");
    map.put("age", "95");

    String expected = "Hello David. You are {$age} years old";
    String actual = formatter.interpolateMessage(illegalMsg, map);

    assertThat(actual).isEqualTo(expected);
  }
}
