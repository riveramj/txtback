package com.riveramj.model

import net.liftweb.mapper._

class Surveyor extends LongKeyedMapper[Surveyor] with IdPK with OneToMany[Long, Surveyor] {
  def getSingleton = Surveyor

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

object Surveyor extends Surveyor with LongKeyedMetaMapper[Surveyor]{}
