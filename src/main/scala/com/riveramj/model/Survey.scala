package com.riveramj.model

import net.liftweb.mapper._

class Survey extends LongKeyedMapper[Survey] with IdPK with OneToMany[Long, Survey] {
  def getSingleton = Survey

  object surveyId extends MappedString(this, 256){
    override def dbIndexed_? = true
  }
  object surveyName extends MappedString(this, 140) {
    override def dbIndexed_? = true
  }

  object questions extends MappedOneToMany(Question, Question.survey, OrderBy(Question.questionId, Ascending))
  object company extends MappedLongForeignKey(this, Company)
}

object Survey extends Survey with LongKeyedMetaMapper[Survey]{}
