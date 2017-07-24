package com.softwaremill.sttp.asynchttpclient.scalaz

import com.softwaremill.sttp.asynchttpclient.{
  AsyncHttpClientHandler,
  MonadAsyncError
}
import org.asynchttpclient.{
  AsyncHttpClient,
  AsyncHttpClientConfig,
  DefaultAsyncHttpClient
}

import scalaz.{-\/, \/-}
import scalaz.concurrent.Task

class ScalazAsyncHttpClientHandler private (asyncHttpClient: AsyncHttpClient)
    extends AsyncHttpClientHandler[Task](asyncHttpClient, TaskMonad)

object ScalazAsyncHttpClientHandler {
  def apply(): ScalazAsyncHttpClientHandler =
    new ScalazAsyncHttpClientHandler(new DefaultAsyncHttpClient())
  def usingConfig(cfg: AsyncHttpClientConfig): ScalazAsyncHttpClientHandler =
    new ScalazAsyncHttpClientHandler(new DefaultAsyncHttpClient())
  def usingClient(client: AsyncHttpClient): ScalazAsyncHttpClientHandler =
    new ScalazAsyncHttpClientHandler(client)
}

private[scalaz] object TaskMonad extends MonadAsyncError[Task] {
  override def unit[T](t: T): Task[T] = Task.point(t)

  override def map[T, T2](fa: Task[T], f: (T) => T2): Task[T2] = fa.map(f)

  override def flatMap[T, T2](fa: Task[T], f: (T) => Task[T2]): Task[T2] =
    fa.flatMap(f)

  override def async[T](
      register: ((Either[Throwable, T]) => Unit) => Unit): Task[T] =
    Task.async { cb =>
      register {
        case Left(t)  => cb(-\/(t))
        case Right(t) => cb(\/-(t))
      }
    }

  override def error[T](t: Throwable): Task[T] = Task.fail(t)
}