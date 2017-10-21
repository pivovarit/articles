package com.pivovarit.hamming.vavr;

import io.vavr.collection.Stream;

class HammingHelper {

    int codewordSize(int msgLength) {
        return Stream.from(2, 1)
          .filter(r -> msgLength + r + 1 <= 1 << r)
          .map(r -> r + msgLength)
          .head();
    }

    Stream<Integer> getHammingCodewordIndices(int msgLength) {
        return Stream.from(0, 1)
          .take(codewordSize(msgLength));
    }

    String getParityBit(int codeWordIndex, BinaryString msg) {
        return parityIndicesSequence(codeWordIndex, codewordSize(msg.getValue().length()))
          .map(it -> getDataBit(it, msg))
          .map(Integer::valueOf)
          .reduce((a, b) -> (a + b) % 2)
          .toString();
    }

    String getDataBit(int ind, BinaryString input) {
        return Character.toString(
          input.getValue().charAt(ind - Integer.toBinaryString(ind).length()));
    }

    Stream<Integer> parityIndicesSequence(int startIndex, int endExclusive) {
        return Stream.from(startIndex, 1)
          .take(endExclusive - startIndex)
          .zipWithIndex()
          .filter(t -> (t._2 % ((2 * (startIndex + 1)))) < startIndex + 1)
          .map(t -> t._1)
          .drop(1);
    }
}
