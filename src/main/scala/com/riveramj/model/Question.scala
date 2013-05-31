package com.riveramj.model

import net.liftweb.mapper._

class Question extends LongKeyedMapper[Question] with IdPK with OneToMany[Long, Question] {
  def getSingleton = Question

  object question extends MappedString(this, 140) {
    override def dbIndexed_? = true
  }
  object questionId extends MappedLong(this){
    override def dbIndexed_? = true
  }

  object questionNumber extends MappedLong(this){
    override def dbIndexed_? = true
  }

  object surveyId extends MappedLongForeignKey(this, Survey)
}

object Question extends Question with LongKeyedMetaMapper[Question]{}