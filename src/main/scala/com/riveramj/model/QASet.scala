package com.riveramj.model

import net.liftweb.mapper._

class QASet extends LongKeyedMapper[QASet] with IdPK with OneToMany[Long, QASet] {
  def getSingleton = QASet

  object qaSetId extends MappedLong(this){
    override def dbIndexed_? = true
  }

  object surveyInstanceId extends MappedLongForeignKey(this, SurveyInstance)
  object questionId extends MappedLongForeignKey(this, Question)
  object AnswerId extends MappedLongForeignKey(this, Answer)
}

object QASet extends QASet with LongKeyedMetaMapper[QASet]{}
