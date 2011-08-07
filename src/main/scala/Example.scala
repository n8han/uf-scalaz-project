package com.example

import unfiltered.request._
import unfiltered.response._

import org.clapper.avsl.Logger

/** unfiltered plan */
class App extends unfiltered.filter.Plan {
  import unfiltered.monad._
  import LogLevel._, RequestLogger._, RequestMonad._, DefaultConversions._,
         RequestError._, ParamOps._

  val logger = Logger(classOf[App])

  def intent = {
    case GET(Path(p)) =>
      logger.debug("GET %s" format p)
      Ok ~> view(Map.empty)(<p> What say you? </p>)
    case req@POST(Path(p) & Params(params)) =>
      logger.debug("POST %s" format p)
      val vw = view(params)_
      val expected = for {
        params <- getParams
        message <-
          (params.required[Int]("int") |@|
           params.required[Palindrome]("palindrome")) { (i, p) =>
             <p>Yup. { i } is an integer and { p.value }
                is a palindrome. </p>
           }
      } yield message
      vw(expected(req).over.fold(
        failure = { f =>
          <ul> { f.map {
            case Invalid(f, _) => <li>{f} </li> 
            case Missing(f) => <li>{f} </li> 
          }.list } </ul>
        },
        success = s => <p> { s } </p>
      ))
  }

  implicit val palConversions = new Conversion[Palindrome] {
    def to(s: String):Palindrome =
      if (!s.isEmpty && s.toLowerCase.reverse == s.toLowerCase) Palindrome(s)
      else error("not a palindrome")
    def toSeq(in:Seq[String]):Seq[Palindrome] = in.map(to)
  }

  def view(params: Map[String, Seq[String]])(body: scala.xml.NodeSeq) = {
    def p(k: String) = params.get(k).flatMap { _.headOption } getOrElse("")
    Html(
     <html>
      <head>
        <title>uf example</title>
        <link rel="stylesheet" type="text/css" href="/assets/css/app.css"/>
      </head>
      <body>
       <div id="container">
       { body }
       <form method="POST">
         <div>Integer <input type="text" name="int" value={ p("int") } /></div>
         <div>Palindrome <input type="text" name="palindrome" value={ p("palindrome") } /></div>
         <input type="submit" />
       </form>
       </div>
     </body>
    </html>
   )
  }
}
case class Palindrome(value: String)

/** embedded server */
object Server {
  val logger = Logger(Server.getClass)
  def main(args: Array[String]) {
    val http = unfiltered.jetty.Http.anylocal // this will not be necessary in 0.4.0
    http.context("/assets") { _.resources(new java.net.URL(getClass().getResource("/www/css"), ".")) }
      .filter(new App).run({ svr =>
        unfiltered.util.Browser.open(http.url)
      }, { svr =>
        logger.info("shutting down server")
      })
  }
}
