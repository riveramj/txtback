package com.riveramj.model

import net.liftweb.mapper._

class Question extends LongKeyedMapper[Question] with IdPK with OneToMany[Long, Question] {
  def getSingleton = Question

  object question extends MappedString(this, 140) {
    override def dbIndexed_? = true
  }

  object survey extends MappedLongForeignKey(this, Survey)
  object answers extends MappedOneToMany(Answer, Answer.question, OrderBy(Answer.answerId, Ascending))
}

object Question extends Question with LongKeyedMetaMapper[Question]{}