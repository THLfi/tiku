package fi.thl.pivot.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import com.google.common.collect.Lists;
import com.google.common.io.BaseEncoding;
import com.google.common.io.ByteStreams;

import com.google.common.base.Preconditions;

public class PivotUtils {
  /**
   * <p>
   * The concept of this class is to pack a list of integers to a smaller
   * character string than a basic CSV. This is achieved by
   * </p>
   * <ol>
   * <li>first converting a list of integers to a list of differences between
   * these integers e.g. a list 1, 3, 7 becomes (1 - 0), (3 - 1), (7 - 3) = 1, 2,
   * 4. Especially with larger numbers this may help to reduce number of
   * characters</li>
   * <li>then we convert each integer to it's base64 equivalent that is 10 becomes
   * a. We reserve on character to represent sign</li>
   * <li>thirdly we pack repeating numbers as multiplications e.g. 1,1,1 becomes
   * 3.1</li>
   * <li>as a last step we create a separated string where differences that can be
   * represented by a single character are not separated from each other further
   * reducing the need for extra characters</li>
   * </ol>
   *
   * <p>
   * Using this method a list of integers could be compressed to around 30-40 % of
   * the size of a raw csv. We can further compress the list using gzip and get to
   * around 15 % compressed size when using base64 to represent the zipped string.
   * </p>
   *
   * @author aleksiyrttiaho
   *
   */


    static final String SIGN_MARKER = "-";
    static final int BASE = 64;
    static final String ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPRQSTUVWXYZ/=";


    public static String packAndZip(List<Integer> integers) {
      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DeflaterOutputStream zip = new DeflaterOutputStream(out,
          new Deflater(Deflater.BEST_COMPRESSION, true));
        zip.write(pack(integers).getBytes());
        zip.finish();
        return BaseEncoding.base64Url().encode(out.toByteArray());
      } catch (IOException e) {
        throw new IllegalStateException("Could not pack and zip", e);
      }
    }

    public static List<Integer> unzipAndUnpack(String packed) {
      try {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InflaterInputStream zip = new InflaterInputStream(
          new ByteArrayInputStream(BaseEncoding.base64Url().decode(
            packed)), new Inflater(true));
        ByteStreams.copy(zip, out);
        return unpack(out.toString());
      } catch (IOException e) {
        throw new IllegalStateException("Could not unzip and unpack", e);
      }
    }

    private static String pack(List<Integer> integers) {
      return packTokens(tokenize(asDifferences(integers)));
    }

    private static List<Integer> unpack(String packed) {
      if (packed.length() == 0) {
        return Collections.emptyList();
      }
      return asIntegers(tokensAsDifferences(tokenize(packed)));
    }

    private static List<Integer> tokensAsDifferences(List<String> tokens) {
      List<Integer> differences = Lists.newArrayList();
      for (String token : tokens) {
        if (token.indexOf('.') >= 0) {
          String[] t = token.split("\\.");
          int difference = decode(t[1]);
          for (int i = 0; i < decode(t[0]); ++i) {
            differences.add(difference);
          }
        } else {
          differences.add(decode(token));
        }
      }
      return differences;
    }

    private static List<Integer> asIntegers(List<Integer> differences) {
      List<Integer> integers = Lists.newArrayList();
      int last = 0;
      for (int difference : differences) {
        last += difference;
        integers.add(last);
      }
      return integers;
    }

    private static List<String> tokenize(String packed) {
      List<String> tokens = Lists.newArrayList();
      boolean isSingle = true;
      String token = "";
      for (int i = 0; i < packed.length(); ++i) {
        char c = packed.charAt(i);
        if (c == ';' || c == '_') {
          if (token.length() > 0) {
            tokens.add(token);
          }
          token = "";
        }

        if (c == '_') {
          isSingle = true;
        } else if (c == ';') {
          isSingle = false;
        } else if (isSingle) {
          tokens.add(String.valueOf(c));
        } else {
          token += c;
        }
      }
      if (!isSingle && token.length() > 0) {
        tokens.add(token);
      }
      return tokens;
    }

    private static String packTokens(List<String> tokens) {
      StringBuilder packed = new StringBuilder();
      String lastToken = "1";
      for (String token : tokens) {
        if (token.length() > 1) {
          packed.append(";");
        } else if (lastToken.length() > 1) {
          packed.append("_");
        }
        packed.append(token);
        lastToken = token;
      }
      return packed.toString();
    }

    private static List<String> tokenize(List<String> strings) {
      List<String> tokens = Lists.newArrayList();
      String lastToken = strings.get(0);
      int lastIndex = 0;
      for (int i = 1; i < strings.size(); ++i) {
        final String token = strings.get(i);

        if (lastToken.equals(token)) {
          continue;
        }

        tokens.add(createToken(lastToken, lastIndex, i));

        lastToken = token;
        lastIndex = i;

      }
      tokens.add(createToken(lastToken, lastIndex, strings.size()));
      return tokens;
    }

    private static String createToken(String token, int lastIndex, int i) {
      if (i - lastIndex > 1) {
        return encode(i - lastIndex) + "." + token;
      } else {
        return token;
      }
    }

    private static List<String> asDifferences(List<Integer> integers) {
      List<String> differences = Lists.newArrayList();
      int lastInteger = 0;
      for (int integer : integers) {
        differences.add(encode(integer - lastInteger));
        lastInteger = integer;
      }
      return differences;
    }

  private static String encode(int integer) {
    if (integer == 0) {
      return "0";
    }
    if (integer < 0) {
      return SIGN_MARKER + encode(-integer);
    }
    StringBuilder encoded = new StringBuilder();
    while (integer != 0) {
      int remainder = integer % BASE;
      encoded.append(ALPHABET.charAt(remainder));
      integer = integer / BASE;
    }
    return encoded.reverse().toString();
  }

  private static int decode(String base64) {
    Preconditions.checkNotNull(base64);
    Preconditions.checkArgument(base64.length() > 0);
    if (base64.charAt(0) == '-') {
      return -1 * decode(base64.substring(1));
    }
    int integer = 0;
    for (int i = 0; i < base64.length(); ++i) {
      integer *= BASE;
      int remainder = ALPHABET.indexOf(base64.charAt(i));
      if (remainder == -1) {
        throw new IllegalArgumentException("Input string " + base64
          + " contains illegal character " + base64.charAt(i));
      }
      integer += remainder;
    }
    return integer;
  }
}
