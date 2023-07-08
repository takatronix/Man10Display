/*
 * MIT License
 *
 * Copyright (c) 2021 Brandon Li
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package red.man10.display.dither;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import org.jetbrains.annotations.NotNull;

public final class DitherLookupUtil {

  public static final int[] PALETTE;
  public static final byte[] COLOR_MAP;
  public static final int[] FULL_COLOR_MAP;

  static {
    COLOR_MAP = new byte[128 * 128 * 128];
    FULL_COLOR_MAP = new int[128 * 128 * 128];
    final List<Integer> colors = getPaletteColors();
    PALETTE = new int[colors.size()];
    updateIndices(colors);
    createLookupTable(forkRed());
  }

  private static void createLookupTable(@NotNull final List<LoadRed> tasks) {
    for (int i = 0; i < 128; i++) {
      final byte[] sub = tasks.get(i).join();
      final int ci = i << 14;
      for (int si = 0; si < 16384; si++) {
        COLOR_MAP[ci + si] = sub[si];
        FULL_COLOR_MAP[ci + si] = PALETTE[Byte.toUnsignedInt(sub[si])];
      }
    }
  }

  private static @NotNull List<LoadRed> forkRed() {
    final List<LoadRed> tasks = new ArrayList<>(128);
    for (int r = 0; r < 256; r += 2) {
      final LoadRed red = new LoadRed(PALETTE, r);
      tasks.add(red);
      red.fork();
    }
    return tasks;
  }

  private static void updateIndices(@NotNull final List<Integer> colors) {
    int index = 0;
    for (final int color : colors) {
      PALETTE[index++] = color;
    }
    PALETTE[0] = 0;
  }

  private static @NotNull List<Integer> getPaletteColors() {
    final List<Integer> colors = new ArrayList<>();
    for (int i = 0; i < 256; ++i) {
      try {
        final Color color = MapPalette.getColor((byte) i);
        colors.add(color.getRGB());
      } catch (final IndexOutOfBoundsException e) {
        break;
      }
    }
    return colors;
  }

  public static int[] getPalette() {
    return PALETTE;
  }

  public static byte[] getColorMap() {
    return COLOR_MAP;
  }

  public static int[] getFullColorMap() {
    return FULL_COLOR_MAP;
  }

  /** Init. */
  public static void init() {}
}