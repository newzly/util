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
package com.outworkers.util.testing

import java.util.{Date, Locale, UUID}

import org.joda.time.{DateTime, DateTimeZone, LocalDate}
import org.scalacheck.Gen
import org.fluttercode.datafactory.impl.DataFactory

import scala.util.Random

trait Sample[T] {
  def sample: T
}

object Sample {

  /**
    * !! Warning !! Black magic going on. This will use the excellent macro compat
    * library to macro materialise an instance of the required primitive based on the type argument.
    * @tparam T The type parameter to materialise a sample for.
    * @return A derived sampler, materialised via implicit blackbox macros.
    */
  implicit def materialize[T]: Sample[T] = macro SamplerMacro.materialize[T]

  def apply[T : Sample]: Sample[T] = implicitly[Sample[T]]
}


object Samples extends Generators {
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

  class BooleanSampler extends Sample[Boolean] {
    def sample: Boolean = Random.nextBoolean()
  }

  class IntSampler extends Sample[Int] {
    def sample: Int = Random.nextInt()
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

  class DateTimeSampler extends Sample[DateTime] {
    def sample: DateTime = new DateTime(DateTimeZone.UTC)
  }

  class LocalDateSampler extends Sample[LocalDate] {
    def sample: LocalDate = new LocalDate()
  }

  class UUIDSampler extends Sample[UUID] {
    def sample: UUID = UUID.randomUUID()
  }

  class EmailAddressSampler extends Sample[EmailAddress] {
    def sample: EmailAddress = EmailAddress(new DataFactory().getEmailAddress)
  }

  class FirstNameSampler extends Sample[FirstName] {
    def sample: FirstName = FirstName(new DataFactory().getFirstName)
  }

  class LastNameSampler extends Sample[LastName] {
    def sample: LastName = LastName(new DataFactory().getLastName)
  }

  class FullNameSampler extends Sample[FullName] {
    def sample: FullName = FullName(s"${new DataFactory().getFirstName} ${new DataFactory().getLastName}")
  }

  class CountryCodeSampler extends Sample[CountryCode] {
    def sample: CountryCode = CountryCode(Gen.oneOf(Locale.getISOCountries).sample.get)
  }

  class CountrySampler extends Sample[Country] {
    def sample: Country = Country(Gen.oneOf(CommonDataSamplers.Countries).sample.get)
  }

  class CitySampler extends Sample[City] {
    def sample: City = City(Gen.oneOf(CommonDataSamplers.Cities).sample.get)
  }

  class ProgrammingLanguageSampler extends Sample[ProgrammingLanguage] {
    def sample: ProgrammingLanguage = ProgrammingLanguage(Gen.oneOf(CommonDataSamplers.ProgrammingLanguages).sample.get)
  }

  class LoremIpsumSampler extends Sample[LoremIpsum] {
    def sample: LoremIpsum = LoremIpsum(Gen.oneOf(CommonDataSamplers.LoremIpsum).sample.get)
  }

  class UrlSampler extends Sample[Url] {
    def sample: Url = {
      val str = java.lang.Long.toHexString(java.lang.Double.doubleToLongBits(Math.random()))
      Url(oneOf(protocols) + "://www." + str + "." + oneOf(domains))
    }
  }

}


