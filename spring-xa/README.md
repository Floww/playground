# Synthesis

Just a standalone XA test on Spring with Atomikos, ActiveMQ and Oracle. 
Unit test (incomplete) with HSQL.

# Issues

If there are some errors about datasource XA for Oracle (like "Error in recovery")
Have a look to this link : http://gaob.blogspot.fr/2009/11/error-in-recovery-comatomikosdatasource.html

        grant select on sys.dba_pending_transactions to <user>;
        grant select on sys.pending_trans$ to <user>;
        grant select on sys.dba_2pc_pending to <user>;
        grant execute on sys.dbms_system to <user>;