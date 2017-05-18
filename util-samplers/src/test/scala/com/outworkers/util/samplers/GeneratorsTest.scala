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

import org.scalatest.{ FlatSpec, Matchers }

class GeneratorsTest extends FlatSpec with Matchers {

  it should "generate a sized list based on the given argument" in {
    val limit = 10
    assert(genList[String](limit).size == limit)
  }

  it should "generate a sized map based on the given size argument" in {
    val limit = 10
    assert(genMap[String, String](limit).size == limit)
  }

  it should "generate a sized map of known key and value types" in {
    val limit = 10
    assert(genMap[String, String](limit).size == limit)
  }

  it should "automatically derive valid samples" in {
    val sample = gen[User]
  }

  it should "automatically derive generator samples for complex case classes" in {
    val sample = gen[CollectionSample]
    val user = gen[User]
    val tp = gen[TupleRecord]
    val tpColl = gen[TupleCollectionRecord]
  }

  it should "automatically derive a generator for a nested case class in a different package" in {
    "val sample = gen[NestedOtherPackage]" should compile
  }

  it should "automatically derive a sample for a nested case class" in {
    val sample = gen[NestedUser]
    sample shouldEqual sample
  }


}
