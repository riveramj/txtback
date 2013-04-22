package com.riveramj.model

import net.liftweb.mapper._

class Survey extends LongKeyedMapper[Survey] with OneToMany[Long, Survey] {
  def getSingleton = Survey

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object surveyName extends MappedString(this, 140) {
    override def dbIndexed_? = true
  }

  object questions extends MappedOneToMany(Question, Question.survey, OrderBy(Question.id, Ascending))
  object company extends MappedLongForeignKey(this, Company)
}

object Survey extends Survey with LongKeyedMetaMapper[Survey]{}
