package com.riveramj.model

import net.liftweb.mapper._

class SurveyInstance extends LongKeyedMapper[SurveyInstance] with IdPK with OneToMany[Long, SurveyInstance] {
  def getSingleton = SurveyInstance

  object surveyInstanceId extends MappedLong(this){
    override def dbIndexed_? = true
  }

  object responderPhone extends MappedString(this, 140)
  object status extends MappedLong(this) // 1 = started; 2 = finished
  object dateStarted extends MappedDateTime(this)

  object qaSet extends MappedOneToMany(QASet, QASet.SurveyInstanceId, OrderBy(QASet.id, Ascending))

  object SurveyId extends MappedLongForeignKey(this, Survey)
  object currentQuestionId extends MappedLongForeignKey(this, Question)
}

object SurveyInstance extends SurveyInstance with LongKeyedMetaMapper[SurveyInstance]{}
