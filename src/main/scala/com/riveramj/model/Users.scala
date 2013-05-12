package com.riveramj.model

import net.liftweb.mapper._

class Users extends LongKeyedMapper[Users] with IdPK with OneToMany[Long, Users] {
  def getSingleton = Users

  object userId extends MappedLong(this) {
    override def dbIndexed_? = true
  }
  object firstName extends MappedString(this, 64){override def defaultValue = ""}
  object lastName extends MappedString(this, 64){override def defaultValue = ""}
  object email extends MappedString(this, 128){override def defaultValue = ""}
  object password extends MappedString(this, 256)
  object salt extends MappedString(this, 256)

  object companyId extends MappedLongForeignKey(this, Company)
}

object Users extends Users with LongKeyedMetaMapper[Users]{}
