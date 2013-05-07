package com.riveramj.model

import net.liftweb.mapper._

class Company extends LongKeyedMapper[Company] with IdPK with OneToMany[Long, Company] {
  def getSingleton = Company

  object companyId extends MappedLong(this){
    override def dbIndexed_? = true
  }
  object companyName extends MappedString(this, 140) {
    override def dbIndexed_? = true
  }

  object surveys extends MappedOneToMany(Survey, Survey.company, OrderBy(Survey.id, Ascending))
  object users extends MappedOneToMany(User, User.companyId, OrderBy(User.id, Ascending))
}

object Company extends Company with LongKeyedMetaMapper[Company]{}
