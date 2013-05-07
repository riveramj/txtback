package com.riveramj.model

import net.liftweb.mapper._

class User extends LongKeyedMapper[User] with IdPK with OneToMany[Long, User] {
  def getSingleton = User

  object userId extends MappedLong(this) {
    override def dbIndexed_? = true
  }
  object firstName extends MappedString(this, 64){override def defaultValue = ""}
  object lastName extends MappedString(this, 64){override def defaultValue = ""}
  object email extends MappedString(this, 64){override def defaultValue = ""}
  object password extends MappedString(this, 256)
  object salt extends MappedString(this, 256)

  object companyId extends MappedLongForeignKey(this, Company)
}

object User extends User with LongKeyedMetaMapper[User]{}
