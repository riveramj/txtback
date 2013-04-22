package com.riveramj.model

import net.liftweb.mapper._

class Question extends LongKeyedMapper[Question] with OneToMany[Long, Question] {
  def getSingleton = Question

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object question extends MappedString(this, 140) {
    override def dbIndexed_? = true
  }

  object survey extends MappedLongForeignKey(this, Survey)
  object answers extends MappedOneToMany(Answer, Answer.question, OrderBy(Answer.id, Ascending))
}

object Question extends Question with LongKeyedMetaMapper[Question]{}