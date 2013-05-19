package com.riveramj.service

import net.liftweb.common._
import com.riveramj.model.QASet
import net.liftweb.mapper.By
import org.joda.time.DateTime
import net.liftweb.util.Helpers._
import com.riveramj.util.RandomIdGenerator._

object QASetService extends Loggable {

  def createQASet(surveyInstanceId: Long, questionId: Long, answerId: Long) = {
    val qaSet = QASet.create
      .qaSetId(generateLongId())
      .SurveyInstanceId(surveyInstanceId)
      .QuestionId(questionId)
      .AnswerId(answerId)

    tryo(saveQASet(qaSet)) flatMap {
      u => u match {
        case Full(newQASet:QASet) => Full(newQASet)
        case (failure: Failure) => failure
        case _ => Failure("Unknown error")
      }
    }
  }

  def saveQASet(qaSet:QASet):Box[QASet] = {

    val validateErrors = qaSet.validate

    if (validateErrors.isEmpty) {
      tryo(qaSet.saveMe()) match {
        case Full(newQASet:QASet) =>
          Full(newQASet)
        case Failure(_, Full(err), _) =>
          logger.error("save qaSet failed with %s" format err)
          Failure("Unknown error")
        case _ =>
          Failure("Unknown error")
      }
    } else {
      Failure("Validations Failed")
    }
  }

  def getQASetById(qaSetId: Long): Box[QASet] = {
    QASet.find(By(QASet.qaSetId, qaSetId))
  }

  def deleteQASetById(qaSetId: Long): Box[Boolean] = {
    val qaSet = QASet.find(By(QASet.qaSetId, qaSetId))
    qaSet.map(_.delete_!)
  }

  def findAllSurveyQASets(qaSetId:Long): List[QASet] = {
    QASet.findAll(By(QASet.qaSetId, qaSetId))
  }

  def getAllQASets = {
    QASet.findAll()
  }

}
