package com.riveramj.model

import net.liftweb.mapper._

class Company extends LongKeyedMapper[Company] with OneToMany[Long, Company] {
  def getSingleton = Company

  def primaryKeyField = id
  object id extends MappedLongIndex(this)
  object companyName extends MappedString(this, 140) {
    override def dbIndexed_? = true
  }

  object surveys extends MappedOneToMany(Survey, Survey.company, OrderBy(Survey.id, Ascending))
}

object Company extends Company with LongKeyedMetaMapper[Company]{}
