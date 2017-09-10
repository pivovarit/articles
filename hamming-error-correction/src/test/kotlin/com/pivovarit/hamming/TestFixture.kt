package com.pivovarit.hamming

import org.pivovarit.hamming.message.BinaryString
import org.pivovarit.hamming.message.EncodedString

internal fun exampleData() = listOf(
  "1" to "111",
  "01" to "10011",
  "11" to "01111",
  "1001000" to "00110010000",
  "1100001" to "10111001001",
  "1101101" to "11101010101",
  "1101001" to "01101011001",
  "1101110" to "01101010110",
  "1100111" to "01111001111",
  "0100000" to "10011000000",
  "1100011" to "11111000011",
  "1101111" to "10101011111",
  "1100100" to "11111001100",
  "1100101" to "00111000101",
  "10011010" to "011100101010")
  .map { BinaryString(it.first) to EncodedString(it.second) }