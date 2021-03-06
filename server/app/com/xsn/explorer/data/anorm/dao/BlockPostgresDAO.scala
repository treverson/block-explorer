package com.xsn.explorer.data.anorm.dao

import java.sql.Connection

import anorm._
import com.xsn.explorer.data.anorm.parsers.BlockParsers._
import com.xsn.explorer.models.Blockhash
import com.xsn.explorer.models.rpc.Block

class BlockPostgresDAO {

  def upsert(block: Block)(implicit conn: Connection): Option[Block] = {
    SQL(
      """
        |INSERT INTO blocks
        |  (
        |    blockhash, previous_blockhash, next_blockhash, tpos_contract, merkle_root, size,
        |    height, version, time, median_time, nonce, bits, chainwork, difficulty
        |  )
        |VALUES
        |  (
        |    {blockhash}, {previous_blockhash}, {next_blockhash}, {tpos_contract}, {merkle_root}, {size},
        |    {height}, {version}, {time}, {median_time}, {nonce}, {bits}, {chainwork}, {difficulty}
        |  )
        |ON CONFLICT (blockhash)
        |DO UPDATE
        |  SET previous_blockhash = EXCLUDED.previous_blockhash,
        |      next_blockhash = EXCLUDED.next_blockhash,
        |      tpos_contract = EXCLUDED.tpos_contract,
        |      merkle_root = EXCLUDED.merkle_root,
        |      size = EXCLUDED.size,
        |      height = EXCLUDED.height,
        |      version = EXCLUDED.version,
        |      time = EXCLUDED.time,
        |      median_time = EXCLUDED.median_time,
        |      nonce = EXCLUDED.nonce,
        |      bits = EXCLUDED.bits,
        |      chainwork = EXCLUDED.chainwork,
        |      difficulty = EXCLUDED.difficulty
        |RETURNING blockhash, previous_blockhash, next_blockhash, tpos_contract, merkle_root, size,
        |          height, version, time, median_time, nonce, bits, chainwork, difficulty
      """.stripMargin
    ).on(
      'blockhash -> block.hash.string,
      'previous_blockhash -> block.previousBlockhash.map(_.string),
      'next_blockhash -> block.nextBlockhash.map(_.string),
      'tpos_contract -> block.tposContract.map(_.string),
      'merkle_root -> block.merkleRoot.string,
      'size -> block.size.int,
      'height -> block.height.int,
      'version -> block.version,
      'time -> block.time,
      'median_time -> block.medianTime,
      'nonce -> block.nonce,
      'bits -> block.bits,
      'chainwork -> block.chainwork,
      'difficulty -> block.difficulty
    ).as(parseBlock.singleOpt).flatten
  }

  def getBy(blockhash: Blockhash)(implicit conn: Connection): Option[Block] = {
    SQL(
      """
        |SELECT blockhash, previous_blockhash, next_blockhash, tpos_contract, merkle_root, size,
        |       height, version, time, median_time, nonce, bits, chainwork, difficulty
        |FROM blocks
        |WHERE blockhash = {blockhash}
      """.stripMargin
    ).on(
      "blockhash" -> blockhash.string
    ).as(parseBlock.singleOpt).flatten
  }

  def delete(blockhash: Blockhash)(implicit conn: Connection): Option[Block] = {
    SQL(
      """
        |DELETE FROM blocks
        |WHERE blockhash = {blockhash}
        |RETURNING blockhash, previous_blockhash, next_blockhash, tpos_contract, merkle_root, size,
        |          height, version, time, median_time, nonce, bits, chainwork, difficulty
      """.stripMargin
    ).on(
      "blockhash" -> blockhash.string
    ).as(parseBlock.singleOpt).flatten
  }

  def getLatestBlock(implicit conn: Connection): Option[Block] = {
    SQL(
      """
        |SELECT blockhash, previous_blockhash, next_blockhash, tpos_contract, merkle_root, size,
        |       height, version, time, median_time, nonce, bits, chainwork, difficulty
        |FROM blocks
        |ORDER BY height DESC
        |LIMIT 1
      """.stripMargin
    ).as(parseBlock.singleOpt).flatten
  }

  def getFirstBlock(implicit conn: Connection): Option[Block] = {
    SQL(
      """
        |SELECT blockhash, previous_blockhash, next_blockhash, tpos_contract, merkle_root, size,
        |       height, version, time, median_time, nonce, bits, chainwork, difficulty
        |FROM blocks
        |ORDER BY height
        |LIMIT 1
      """.stripMargin
    ).as(parseBlock.singleOpt).flatten
  }
}
