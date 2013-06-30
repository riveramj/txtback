package com.riveramj.model

import net.liftweb.mapper._

class Answer extends LongKeyedMapper[Answer] with IdPK {
  def getSingleton = Answer

  object answerId extends MappedLong(this){
    override def dbIndexed_? = true
  }
  object QuestionId extends MappedLongForeignKey(this, Question)
  object answerNumber extends MappedInt(this)
  object answer extends MappedString(this, 140)
}

object Answer extends Answer with LongKeyedMetaMapper[Answer]{}

