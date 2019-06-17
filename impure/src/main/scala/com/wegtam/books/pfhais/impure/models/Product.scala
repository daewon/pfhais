/*
 * CC0 1.0 Universal (CC0 1.0) - Public Domain Dedication
 *
 *                                No Copyright
 *
 * The person who associated a work with this deed has dedicated the work to
 * the public domain by waiving all of his or her rights to the work worldwide
 * under copyright law, including all related and neighboring rights, to the
 * extent allowed by law.
 */

package com.wegtam.books.pfhais.impure.models

import java.util.UUID

import cats.data.NonEmptyList
import cats.implicits._
import eu.timepit.refined.auto._
import io.circe._

/**
  * A product.
  *
  * @param id    The unique ID of the product.
  * @param names A list of translations of the product name.
  */
final case class Product(id: ProductId, names: NonEmptyList[Translation])

object Product {

  implicit val decode: Decoder[Product] = Decoder.forProduct2("id", "names")(Product.apply)

  implicit val encode: Encoder[Product] = Encoder.forProduct2("id", "names")(p => (p.id, p.names))

  /**
    * Try to create a Product from the given list of database rows.
    *
    * @param rows The database rows describing a product and its translations.
    * @return An option to the successfully created Product.
    */
  def fromDatabase(rows: Seq[(UUID, String, String)]): Option[Product] = {
    val po = for {
      (id, c, n) <- rows.headOption
      t          <- Translation.fromUnsafe(c)(n)
      p          <- Product(id = id, names = NonEmptyList.one(t)).some
    } yield p
    po.map(
      p =>
        rows.foldLeft(p) { (a, cols) =>
          val (id, c, n) = cols
          Translation.fromUnsafe(c)(n).fold(a)(t => a.copy(names = a.names :+ t))
      }
    )
  }

}
