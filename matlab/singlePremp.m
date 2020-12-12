tocs = zeros(1,10);
for ii = 0:9
    tic;
    fname = strcat('totalserver_1_trial_', int2str(ii), '.txt')
    val = jsondecode(fileread(fname));
    n = val.config.totalJobs;
    m = val.config.totalServers;
    dn = val.config.totalDuration;
    wn = val.config.maxWorkload;
    a = zeros(1, n);
    d = zeros(1, n);
    c = zeros(1, n);
    for k = 1:dn
        a(k) = val.jobs(k).arrivalTime+1;
        d(k) = val.jobs(k).deadline+1;
        c(k) = val.jobs(k).workload;
    end
    p = 10.0;
    q = 1.0;
    r = 0.1;
    cvx_begin quiet
        variable x(n, dn) nonnegative
        expression y(n)
        expression z(n)
        for k = 1:n
            y(k) = subplus(c(k) - sum(x(k,a(k):d(k)), 2));
            z(k) = subplus(c(k) - sum(x(k,a(k):dn), 2));
        end
        minimize( p * sum(y) + q * sum(z) + r * sum(sum(x, 2), 1) );
        subject to
            sum(x,1) <= 1
    cvx_end
    prev = -1;
    l = 0;
    while abs(cvx_optval-prev) > 0.01
        prev = cvx_optval;
        t = 1 ./ (y + 0.01);
        u = 1 ./ (z + 0.01);
        v = 1 ./ (x + 0.01);
        cvx_begin quiet
            variable x(n, dn) nonnegative
            expression y(n)
            expression z(n)
            for k = 1:n
                y(k) = subplus(c(k) - sum(x(k,a(k):d(k)), 2));
                z(k) = subplus(c(k) - sum(x(k,a(k):dn), 2));
            end
            minimize( p * sum(t .* y) + q * sum(u .* z) + r * sum(sum(v .* x, 2), 1) );
            subject to
                sum(x,1) <= 1
        cvx_end
        l = l + 1;
        fprintf("%d: %f y=%d z=%d x=%d\n", l, cvx_optval, sum(y > 0.01), sum(z > 0.01), sum(x > 0.1, 'all') - sum(x > 0.9, 'all'));
    end
    fid = fopen(strcat('totalserver_1_trial_', int2str(ii), '_result_preemp.txt'),'wt');
    fprintf(fid,'[');
    for jj = 1:n-1
        fprintf(fid,'[');
        fprintf(fid,'%g,',round(x(jj,1:dn-1)));
        fprintf(fid,'%g',round(x(jj,dn)));
        fprintf(fid,'],');
    end
    fprintf(fid,'[');
    fprintf(fid,'%g,',round(x(n,1:dn-1)));
    fprintf(fid,'%g',round(x(n,dn)));
    fprintf(fid,']');
    fprintf(fid,']');
    fclose(fid);
    tocs(ii+1)= toc
end