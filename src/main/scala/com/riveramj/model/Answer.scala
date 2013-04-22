package com.riveramj.model

import net.liftweb.mapper._

class Answer extends LongKeyedMapper[Answer] {
  def getSingleton = Answer

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object question extends MappedLongForeignKey(this, Question)
  object answer extends MappedString(this, 140)
}

object Answer extends Answer with LongKeyedMetaMapper[Answer]{}

