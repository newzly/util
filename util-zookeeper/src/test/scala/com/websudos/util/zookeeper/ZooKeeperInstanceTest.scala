/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.util.zookeeper

import java.net.{InetAddress, InetSocketAddress}
import java.util.concurrent.TimeUnit

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}
import com.websudos.util.testing._

import scala.concurrent.duration.Duration


class ZooKeeperInstanceTest extends FlatSpec with Matchers with BeforeAndAfterAll {

  implicit val patience: PatienceConfiguration.Timeout = timeout(Duration(3L, TimeUnit.SECONDS))

  val path = "/" + gen[String]
  val instance = new ZooKeeperInstance(path)

  override def beforeAll(): Unit = {
    super.beforeAll()
    instance.start()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    instance.stop()
  }

  it should "correctly set the status flag to true after starting the ZooKeeper Instance" in {
    instance.isStarted shouldEqual true
  }

  it should "correctly initialise a ZooKeeper ServerSet after starting a ZooKeeper instance" in {
    instance.zookeeperServer.isRunning shouldEqual true
  }

  ignore should "allow setting a value for the path" in {

    val data = gen[String]

    val chain = for {
      set <- instance.client.setData(path, data.getBytes, -1)
      get <- instance.client.getData(path, watch = false)
    } yield get


    chain.successful {
      res => {
        new String(res.data) shouldEqual data
      }
    }
  }

  ignore should "correctly parse the retrieved data into a Sequence of InetSocketAddresses" in {

    val data = InetAddress.getLocalHost
    val port = 1001
    val address = s"${data.getHostName}:$port"

    val chain = for {
      set <- instance.client.setData(path, address.getBytes, -1)
      get <-  instance.hostnamePortPairs
    } yield get

    chain.successful {
      res => {
        res shouldEqual Seq(new InetSocketAddress(data.getHostName, port))
      }
    }
  }
}
