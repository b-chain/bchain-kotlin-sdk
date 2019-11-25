#include "sysApi.h"

class database
{
public:
    database() {}
    void set(char* key, char* val, unsigned int valLen, unsigned long long blkNumber, unsigned int expiry);
    void get(char* key);
};

static void blkNumberValidate(unsigned long long blkNumber, unsigned int expiry)
{
    unsigned long long CurNumber = block_number();
    assert(((CurNumber >= blkNumber) && (CurNumber <= blkNumber+expiry)), "action expired");
}

void database::set(char* key, char* val, unsigned int vallen, unsigned long long blkNumber, unsigned int expiry)
{
    blkNumberValidate(blkNumber, expiry);
    char sender[48];
    action_sender(sender);
    requireAuth(sender);
    int keylen = strlen(key);
    assert(keylen < 1024 && keylen > 0, "key len zero or exceed");
    assert(vallen > 0 && vallen < 16*1024, "val len zero or exceed");

    db_set(key, keylen, val, vallen);
}

void database::get(char* key)
{
    char val[16*1024];
    int valLen = db_get(key, strlen(key), val);
    if (valLen != 0) {
        setResult(val, valLen);
    }
}

extern "C"
{
    static database db;
    void set(char* key, char* val, unsigned int len, unsigned long long blkNumber, unsigned int expiry)
    {
        return db.set(key, val, len, blkNumber, expiry);
    }
    void get(char* key)
    {
        return db.get(key);
    }
}

#define BCHAINIO_ABI(type, name)
BCHAINIO_ABI(database, (set)(get))
