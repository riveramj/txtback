package com.riveramj.model

import net.liftweb.mapper._

class SurveyInstance extends LongKeyedMapper[SurveyInstance] with IdPK with OneToMany[Long, SurveyInstance] {
  def getSingleton = SurveyInstance

  object surveyInstanceId extends MappedLong(this){
    override def dbIndexed_? = true
  }

  object responderPhone extends MappedString(this, 140)

  object qaSet extends MappedOneToMany(QASet, QASet.SurveyInstanceId, OrderBy(QASet.id, Ascending))

  object SurveyId extends MappedLongForeignKey(this, Survey)
}

object SurveyInstance extends SurveyInstance with LongKeyedMetaMapper[SurveyInstance]{}
