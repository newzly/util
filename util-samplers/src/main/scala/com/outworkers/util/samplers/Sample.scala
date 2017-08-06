/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
package com.outworkers.util.samplers

import java.net.InetAddress
import java.util.{Date, Locale, UUID}

import org.scalacheck.{Arbitrary, Gen}

import scala.collection.generic.CanBuildFrom
import scala.util.Random

trait Sample[T] {
  def sample: T
}

object Sample {

  def derive[T : Sample, T1](fn: T => T1): Sample[T1] = new Sample[T1] {
    override def sample: T1 = fn(gen[T])
  }

  def arbitrary[T : Sample]: Arbitrary[T] = Arbitrary(generator[T])

  def generator[T : Sample]: Gen[T] = Gen.delay(gen[T])

  /**
    * !! Warning !! Black magic going on. This will use the excellent macro compat
    * library to macro materialise an instance of the required primitive based on the type argument.
    * @tparam T The type parameter to materialise a sample for.
    * @return A derived sampler, materialised via implicit blackbox macros.
    */
  implicit def materialize[T]: Sample[T] = macro SamplerMacro.materialize[T]

  def collection[M[X] <: TraversableOnce[X], T : Sample](
    implicit cbf: CanBuildFrom[Nothing, T, M[T]]
  ): Sample[M[T]] = {
    new Sample[M[T]] {
      override def sample: M[T] = {
        val builder = cbf()
        builder.sizeHint(com.outworkers.util.samplers.defaultGeneration)
        for (_ <- 1 to defaultGeneration) builder += gen[T]
        builder.result()
      }
    }
  }

  def apply[T : Sample]: Sample[T] = implicitly[Sample[T]]
}


object Samples extends Generators {

  private[this] val byteLimit = 127
  private[this] val shortLimit = 256
  private[this] val inetBlock = 4

  class StringSampler extends Sample[String] {
    /**
      * Get a unique random generated string.
      * This uses the default java GUID implementation.
      * @return A random string with 64 bits of randomness.
      */
    def sample: String = UUID.randomUUID().toString
  }

  class ShortStringSampler extends Sample[ShortString] {
    def sample: ShortString = {
      ShortString(java.lang.Long.toHexString(java.lang.Double.doubleToLongBits(Math.random())))
    }
  }

  class ByteSampler extends Sample[Byte] {
    def sample: Byte = Random.nextInt(byteLimit).toByte
  }

  class BooleanSampler extends Sample[Boolean] {
    def sample: Boolean = Random.nextBoolean()
  }

  class IntSampler extends Sample[Int] {
    def sample: Int = Random.nextInt()
  }

  class ShortSampler extends Sample[Short] {
    def sample: Short = Random.nextInt(shortLimit).toShort
  }

  class DoubleSampler extends Sample[Double] {
    def sample: Double = Random.nextDouble()
  }

  class FloatSampler extends Sample[Float] {
    def sample: Float = Random.nextFloat()
  }

  class LongSampler extends Sample[Long] {
    def sample: Long = Random.nextLong()
  }

  class BigDecimalSampler extends Sample[BigDecimal] {
    def sample: BigDecimal = BigDecimal(Random.nextDouble())
  }

  class BigIntSampler extends Sample[BigInt] {
    def sample: BigInt = BigInt(Random.nextLong())
  }

  class DateSampler extends Sample[Date] {
    def sample: Date = new Date()
  }

  class UUIDSampler extends Sample[UUID] {
    def sample: UUID = UUID.randomUUID()
  }

  class EmailAddressSampler extends Sample[EmailAddress] {
    def sample: EmailAddress = {
      val random = new Random
      val test = random.nextInt(100)
      var email: String = ""
      if (test < 50) {
        // name and initial
        email = Generators.oneOf(NameValues.firstNames).charAt(0) +  Generators.oneOf(NameValues.lastNames)
      }
      else {
        // 2 words
        email = Generators.oneOf(ContentDataValues.words) + Generators.oneOf(ContentDataValues.words)
      }

      if (random.nextInt(100) > 80) email = email + random.nextInt(100)
      email = email + "@" + Generators.oneOf(ContentDataValues.emailHosts) + "." + Generators.oneOf(ContentDataValues.tlds)
      EmailAddress(email.toLowerCase)
    }
  }

  class FirstNameSampler extends Sample[FirstName] {
    def sample: FirstName = FirstName(Generators.oneOf(NameValues.firstNames))
  }

  class LastNameSampler extends Sample[LastName] {
    def sample: LastName = LastName(Generators.oneOf(NameValues.lastNames))
  }

  class FullNameSampler extends Sample[FullName] {
    def sample: FullName = FullName(s"${Gen.oneOf(NameValues.firstNames).sample.get} ${Gen.oneOf(NameValues.lastNames).sample.get}")
  }

  class CountryCodeSampler extends Sample[CountryCode] {
    def sample: CountryCode = CountryCode(Gen.oneOf(Locale.getISOCountries).sample.get)
  }

  class CountrySampler extends Sample[Country] {
    def sample: Country = Country(Gen.oneOf(BaseSamplers.Countries).sample.get)
  }

  class CitySampler extends Sample[City] {
    def sample: City = City(Gen.oneOf(BaseSamplers.cities).sample.get)
  }

  class InetAddressSampler extends Sample[InetAddress] {
    def sample: InetAddress = {
      InetAddress.getByAddress(List.tabulate(inetBlock)(_ => new ByteSampler().sample).toArray)
    }
  }

  class ProgrammingLanguageSampler extends Sample[ProgrammingLanguage] {
    def sample: ProgrammingLanguage = ProgrammingLanguage(Gen.oneOf(BaseSamplers.ProgrammingLanguages).sample.get)
  }

  class LoremIpsumSampler extends Sample[LoremIpsum] {
    def sample: LoremIpsum = LoremIpsum(Gen.oneOf(BaseSamplers.LoremIpsum).sample.get)
  }

  class UrlSampler extends Sample[Url] {
    def sample: Url = {
      val str = java.lang.Long.toHexString(java.lang.Double.doubleToLongBits(Math.random()))
      Url(oneOf(protocols) + "://www." + str + "." + oneOf(domains))
    }
  }

}
