package com.riveramj.model

import net.liftweb.mapper._

class SurveyInstance extends LongKeyedMapper[SurveyInstance] with IdPK with OneToMany[Long, SurveyInstance] {
  def getSingleton = SurveyInstance

  object SurveyInstanceId extends MappedLong(this){
    override def dbIndexed_? = true
  }

  object respondeePhone extends MappedString(this, 140)
  object dateStarted extends MappedDateTime(this)
  object dateFinished extends MappedDateTime(this)

  object qaSet extends MappedOneToMany(QASet, QASet.surveyInstanceId, OrderBy(QASet.id, Ascending))

  object SurveyId extends MappedLongForeignKey(this, Survey)
}

object SurveyInstance extends SurveyInstance with LongKeyedMetaMapper[SurveyInstance]{}
