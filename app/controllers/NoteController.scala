package controllers

import javax.inject.Singleton
import javax.inject.Inject
import play.api.mvc._
import play.api.mvc.ControllerComponents
import scala.concurrent.ExecutionContext
import play.api.mvc.AbstractController
import model.NoteDto
import service.INoteService
import scala.concurrent.Future
import play.api.libs.json.Json
import model.Note
import model.NoteLabel
import java.nio.file.Paths

@Singleton
class NoteController @Inject() (noteService: INoteService, cc: ControllerComponents)(implicit ec: ExecutionContext) extends AbstractController(cc) {

  def createNote() = Action.async { implicit request: Request[AnyContent] =>
    var token = request.headers.get("Headers").get
    request.body.asJson.map { json =>
      var note: NoteDto = json.as[NoteDto]
      noteService.createNote(note, token) map { createnoteFuture =>
        Ok(createnoteFuture)
      }
    }.getOrElse(Future {
      BadRequest("User has made a bad request")
    })
  }

  def deleteNote(noteId: Int) = Action.async { implicit request: Request[AnyContent] =>
    var token = request.headers.get("Headers").get
    noteService.deleteNote(noteId, token) map { deleteFuture =>
      //   Ok(deleteFuture)
      deleteFuture match {
        case "DeleteSuccess"    => Ok("Delete success..........")
        case "DeleteNotSuccess" => Conflict("Delete failure.........")
      }
    }
  }

  def updateNote(noteId: Int) = Action.async { implicit request: Request[AnyContent] =>
    var token = request.headers.get("Headers").get
    println("Update nOte " + token)
    request.body.asJson.map { json =>
      var note: NoteDto = json.as[NoteDto]
      noteService.updateNote(noteId, token, note) map { updateFuture =>
        updateFuture match {
          case success => Ok("Update success............")
        }
      }
    }.getOrElse(Future {
      BadRequest("User has made a bad request")
    })
  }

  def getNotes() = Action.async { implicit request: Request[AnyContent] =>
    var token = request.headers.get("Headers").get
    noteService.getNotes(token) map { notes =>
      notes
      println(notes)
      Ok(Json.toJson(notes))
    }
  }

  def addNoteLabel() = Action.async { implicit request: Request[AnyContent] =>
    request.body.asJson.map { json =>
      var noteLabel: NoteLabel = json.as[NoteLabel]
      noteService.addnoteLabel(noteLabel) map { addLabelFuture =>
        addLabelFuture match {
          case "CreateSuccess"    => Ok("label added successfully.........")
          case "CreateNotSuccess" => Conflict("Label not added..........")
        }
      }
    }.getOrElse(Future {
      BadRequest("User has made a bad request")
    })

  }

  def getNoteLabel(noteId: Int) = Action.async { implicit request: Request[AnyContent] =>
    noteService.getNoteLabels(noteId) map { notes =>
      notes
      println(notes)
      Ok(Json.toJson(notes))
    }
  }

  def upload = Action(parse.multipartFormData) { request =>
    request.body.file("file").map { picture =>
      print("In upload............+ " + picture)
      // only get the last part of the filename
      // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
      val filename = Paths.get(picture.filename).getFileName
      println("File uplo..aded..................." + filename)
      picture.ref.moveTo(Paths.get(s"/home/bridgeit/Documents/scala-project/PlaySampleProject/todo_app/app/tmp/$filename"), replace = true)
      Ok("http://localhost:9000/image/" + filename)
    }.getOrElse {
      BadRequest("Missing File")
    }
  }

  // def serveUploadedFiles2( file: String ) = Action.async {
  //    implicit request => {
  //      val dicrectoryPath = "/home/bridgeit/Documents/scala-project/PlaySampleProject/todo_app/app/tmp/"+file
  //         val serveFile = new java.io.File(dicrectoryPath)
  //         println(serveFile + "in Server uploade file...........")
  //
  //      controllers.Assets.at( dicrectoryPath, file, false ).apply( request )
  //    }
  //  }

  def serveUploadedFiles2(file: String) = Action {
    Ok.sendFile(
      content = new java.io.File("/home/bridgeit/Documents/scala-project/PlaySampleProject/todo_app/app/tmp/" + file),
      fileName = _ => file)
  }

}