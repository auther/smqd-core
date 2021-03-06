/*
 * Copyright 2018 UANGEL
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.thing2x.smqd.util;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.StringTokenizer;

class FigletFont {

  private final int height;
  private final int heightWithoutDescenders;
  private final int maxLine;
  private final int smushMode;
  private final char font[][][];
  private final String fontName;

  private static Iterator<String> iterator(URL url) throws IOException {
    InputStream conn;
    conn = url.openStream();
    BufferedReader data = new BufferedReader(new InputStreamReader(new BufferedInputStream(conn)));
    ArrayList<String> list = new ArrayList<String>();
    while (true) {
      String line = data.readLine();
      if (line != null) {
        list.add(line);
      } else {
        break;
      }
    }
    data.close();
    return list.iterator();
  }

  public FigletFont(URL url) throws IOException {
    this(iterator(url));
  }

  public FigletFont(String font) {
    this(Arrays.<String>asList(font.split("\n")).iterator());
  }

  private  FigletFont(Iterator<String> lines) {
    font = new char[256][][];

    String dummyS = lines.next();
    StringTokenizer st = new StringTokenizer(dummyS, " ");
    String s = st.nextToken();
    char hardblank = s.charAt(s.length() - 1);
    height = Integer.parseInt(st.nextToken());
    heightWithoutDescenders = Integer.parseInt(st.nextToken());
    maxLine = Integer.parseInt(st.nextToken());
    smushMode = Integer.parseInt(st.nextToken());
    int dummyI = Integer.parseInt(st.nextToken());

      /* try to read the font name as the first word of
         the first comment line, but this is not standardized ! */
    st = new StringTokenizer(lines.next(), " ");
    if (st.hasMoreElements())
      fontName = st.nextToken();
    else
      fontName = "";

    for (int i = 0;i < dummyI - 1;i++) // skip the comments
      dummyS = lines.next();
    for (int i = 32;i < 256;i++) {  // for all the characters
      //System.out.print(i+":");
      for (int h = 0;h < height;h++) {
        dummyS = lines.hasNext() ? lines.next() : null;
        if (dummyS == null)
          i = 256;
        else {
          //System.out.println(dummyS);
          int iNormal = i;
          boolean abnormal = true;
          if (h == 0) {
            try {
              i = Integer.parseInt(dummyS);
            }
            catch (NumberFormatException e) {
              abnormal = false;
            }
            if (abnormal)
              dummyS = lines.next();
            else
              i = iNormal;
          }
          if (h == 0)
            font[i] = new char[height][];
          int t = dummyS.length() - 1 - ((h == height - 1) ? 1 : 0);
          if (height == 1)
            t++;
          font[i][h] = new char[t];
          for (int l = 0;l < t;l++) {
            char a = dummyS.charAt(l);
            font[i][h][l] = (a == hardblank) ? ' ' : a;
          }
        }
      }
    }
  }

  public int getHeight() {
    return height;
  }

  public int getMaxLine() {
    return maxLine;
  }

  public int getSmushMode() {
    return smushMode;
  }

  public int getHeightWithoutDescenders() {
    return heightWithoutDescenders;
  }

  public String getFontName() {
    return fontName;
  }

  public String getCharLineString(int c, int l) {
    if (font[c][l] == null)
      return null;
    else
      return new String(font[c][l]);
  }

  /**
   * move a banner to the right (for centering)
   */
  private static String scroll(String message, int offset) {
    String result = "";
    String shift = "";
    for (int i = 0; i < offset; i++)
      shift += ' ';
    StringTokenizer st = new StringTokenizer(message, "\n");
    while (st.hasMoreElements())
      result += shift + st.nextToken() + '\n';
    return result;
  }

  /**
   * append a new banner line (center if needed)
   */
  private static String addLine(String text, String line,
                                boolean leftJustify, int splitWidth) {
    String result = text;
    if (leftJustify)
      result += line;
    else
      result += scroll(line, (splitWidth/2 - width(line)/2));
    return result;
  }
  /**
   * the main part, converts from ASCII to a banner,
   * eventually centering each line,
   * folding after each word, or when the width limit is reached
   */
  public String convert(String message, boolean splitAtWord, boolean leftJustify, int splitWidth) {
    String result = "";
    StringTokenizer st = new StringTokenizer(message, " ");
    if (splitAtWord)
      while (st.hasMoreElements())
        result = addLine(result, convertOneLine(st.nextToken()),
                leftJustify, splitWidth);
    else {
      String line = "";
      while (st.hasMoreElements()) {
        String w = st.nextToken(), word;
        if (line.length() == 0)
          word = w;
        else
          word = ' ' + w;
        String newLine = append(line, word);
        if ((width(newLine) > splitWidth) && (line.length() > 0)) {
          result = addLine(result, line + '\n',
                  leftJustify, splitWidth);
          line = append("", w);
        } else
          line = newLine;
      }
      if (line.length() > 0)
        result = addLine(result, line + '\n',
                leftJustify, splitWidth);
    }
    return result;
  }
  /**
   * Gimme the maximum width of a converted text
   */
  public static int width(String message) {
    int w = 0;
    StringTokenizer st = new StringTokenizer(message, "\n");
    while (st.hasMoreElements())
      w = Math.max(w, st.nextToken().length());
    return w;
  }
  /**
   * create a banner from a text
   */
  private String convertOneLine(String message) {
    String result = "";
    for (int l = 0; l < height; l++) { // for each line
      for (int c = 0; c < message.length(); c++) // for each char
        result += getCharLineString((int) message.charAt(c), l);
      result += '\n';
    }
    return result;
  }
  /**
   *  append a word to a banner
   */
  private String append(String message, String end) {
    String result = "";
    int h = 0;
    if (message.length() == 0)
      for (int i = 0; i < height; i++)
        message += " \n";
    StringTokenizer st = new StringTokenizer(message, "\n");
    while (st.hasMoreElements()) {
      result += st.nextToken();
      for (int c = 0; c < end.length(); c++) // for each char
        result += getCharLineString((int) end.charAt(c), h);
      result += '\n';
      h++;
    }
    return result;
  }
}
