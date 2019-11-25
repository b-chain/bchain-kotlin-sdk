(module
  (type $FUNCSIG$vi (func (param i32)))
  (type $FUNCSIG$ii (func (param i32) (result i32)))
  (type $FUNCSIG$vii (func (param i32 i32)))
  (type $FUNCSIG$viiii (func (param i32 i32 i32 i32)))
  (type $FUNCSIG$iiii (func (param i32 i32 i32) (result i32)))
  (type $FUNCSIG$j (func (result i64)))
  (import "env" "action_sender" (func $action_sender (param i32)))
  (import "env" "assert" (func $assert (param i32 i32)))
  (import "env" "block_number" (func $block_number (result i64)))
  (import "env" "db_get" (func $db_get (param i32 i32 i32) (result i32)))
  (import "env" "db_set" (func $db_set (param i32 i32 i32 i32)))
  (import "env" "requireAuth" (func $requireAuth (param i32) (result i32)))
  (import "env" "setResult" (func $setResult (param i32 i32)))
  (import "env" "strlen" (func $strlen (param i32) (result i32)))
  (table 0 anyfunc)
  (memory $0 1)
  (data (i32.const 4) "`\80\00\00")
  (data (i32.const 16) "key len zero or exceed\00")
  (data (i32.const 48) "val len zero or exceed\00")
  (data (i32.const 80) "action expired\00")
  (export "memory" (memory $0))
  (export "_ZN8database3setEPcS0_jyj" (func $_ZN8database3setEPcS0_jyj))
  (export "_ZN8database3getEPc" (func $_ZN8database3getEPc))
  (export "set" (func $set))
  (export "get" (func $get))
  (func $_ZN8database3setEPcS0_jyj (param $0 i32) (param $1 i32) (param $2 i32) (param $3 i32) (param $4 i64) (param $5 i32)
    (local $6 i64)
    (local $7 i32)
    (local $8 i32)
    (i32.store offset=4
      (i32.const 0)
      (tee_local $8
        (i32.sub
          (i32.load offset=4
            (i32.const 0)
          )
          (i32.const 48)
        )
      )
    )
    (set_local $7
      (i32.const 0)
    )
    (block $label$0
      (br_if $label$0
        (i64.lt_u
          (tee_local $6
            (call $block_number)
          )
          (get_local $4)
        )
      )
      (set_local $7
        (i64.le_u
          (get_local $6)
          (i64.add
            (i64.extend_u/i32
              (get_local $5)
            )
            (get_local $4)
          )
        )
      )
    )
    (call $assert
      (get_local $7)
      (i32.const 80)
    )
    (call $action_sender
      (get_local $8)
    )
    (drop
      (call $requireAuth
        (get_local $8)
      )
    )
    (call $assert
      (i32.lt_u
        (i32.add
          (tee_local $7
            (call $strlen
              (get_local $1)
            )
          )
          (i32.const -1)
        )
        (i32.const 1023)
      )
      (i32.const 16)
    )
    (call $assert
      (i32.lt_u
        (i32.add
          (get_local $3)
          (i32.const -1)
        )
        (i32.const 16383)
      )
      (i32.const 48)
    )
    (call $db_set
      (get_local $1)
      (get_local $7)
      (get_local $2)
      (get_local $3)
    )
    (i32.store offset=4
      (i32.const 0)
      (i32.add
        (get_local $8)
        (i32.const 48)
      )
    )
  )
  (func $_ZN8database3getEPc (param $0 i32) (param $1 i32)
    (local $2 i32)
    (i32.store offset=4
      (i32.const 0)
      (tee_local $2
        (i32.sub
          (i32.load offset=4
            (i32.const 0)
          )
          (i32.const 16384)
        )
      )
    )
    (block $label$0
      (br_if $label$0
        (i32.eqz
          (tee_local $1
            (call $db_get
              (get_local $1)
              (call $strlen
                (get_local $1)
              )
              (get_local $2)
            )
          )
        )
      )
      (call $setResult
        (get_local $2)
        (get_local $1)
      )
    )
    (i32.store offset=4
      (i32.const 0)
      (i32.add
        (get_local $2)
        (i32.const 16384)
      )
    )
  )
  (func $set (param $0 i32) (param $1 i32) (param $2 i32) (param $3 i64) (param $4 i32)
    (call $_ZN8database3setEPcS0_jyj
      (get_local $0)
      (get_local $0)
      (get_local $1)
      (get_local $2)
      (get_local $3)
      (get_local $4)
    )
  )
  (func $get (param $0 i32)
    (call $_ZN8database3getEPc
      (get_local $0)
      (get_local $0)
    )
  )
)
