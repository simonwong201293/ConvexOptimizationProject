numlist = [100];
m=10;
cp = zeros(1, m);
fname = 'totalserver_10_server.txt';
val = jsondecode(fileread(fname));
for k = 1:m
    cp(k) = val.servers(k).multiplier;
end
tocs = zeros(length(numlist),10);
for ii = 1:length(numlist)
    for jj = 0:9
        tic;
        fname = strcat('totalserver_', int2str(numlist(ii)), '_trial_', int2str(jj), '.txt')
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
        for t = ceil(max(c)/max(cp))-1:max(d-a)
            tn = min(d, a+t);
            cvx_begin quiet
                variable x(n, m, dn) nonnegative
                expression z(n)
                for k = 1:n
                    z(k) = subplus(c(k) - sum(cp * reshape(x(k,:,a(k):tn(k)), [m tn(k)-a(k)+1]), 2) );
                end
                minimize( sum(z) + 0.001 * sum(sum(sum(x, 3), 2), 1) + 0.00001 * sum(sum(sum(abs(x(:, :, 2:dn) - x(:, :, 1:dn-1)), 3), 2), 1));
                subject to
                    sum(x,2) <= 1;
                    sum(x,1) <= 1;
            cvx_end
            fprintf("%d 0: %f %d\n", t, cvx_optval, sum(sum(x, 3) > 0.2, 'all'));
            if floor(sum(z)) > 0
                continue
            end
            l = 0;
            y = 1 ./ (z + 0.01);
            v = 1 ./ (sum(x, 3) + 0.01);
            xx = 1 ./ (x + 0.01);
            u = 1 ./ (abs(x(:, :, 2:dn) - x(:, :, 1:dn-1)) + 0.01);
            cvx_begin quiet
                variable x(n, m, dn) nonnegative
                expression z(n)
                for k = 1:n
                    z(k) = subplus(c(k) - sum(cp * reshape(x(k,:,a(k):tn(k)), [m tn(k)-a(k)+1]), 2) );
                end
                minimize( sum(y .* z) + 0.001 * sum(sum(sum(xx .* x, 3), 2), 1) + 0.001 * sum(sum(v .* sum(x, 3), 2), 1) + 0.00001 * sum(sum(sum(u .* abs(x(:, :, 2:dn) - x(:, :, 1:dn-1)), 3), 2), 1));
                subject to
                    sum(x,2) <= 1;
                    sum(x,1) <= 1;
            cvx_end
            l = l + 1;
            fprintf("%d %d: %f %d\n", t, l, cvx_optval, sum(sum(x, 3) > 0.2, 'all'));
            prev = -1;
            while abs(cvx_optval-prev) > 0.01
                prev = cvx_optval;
                y = 1 ./ (z + 0.01);
                v = 1 ./ (sum(x, 3) + 0.01);
                xx = 1 ./ (x + 0.01);
                u = 1 ./ (abs(x(:, :, 2:dn) - x(:, :, 1:dn-1)) + 0.01);
                cvx_begin quiet
                    variable x(n, m, dn) nonnegative
                    expression z(n)
                    for k = 1:n
                        z(k) = subplus(c(k) - sum(cp * reshape(x(k,:,a(k):tn(k)), [m tn(k)-a(k)+1]), 2) );
                    end
                    minimize( sum(y .* z) + 0.001 * sum(sum(sum(xx .* x, 3), 2), 1) + 0.001 * sum(sum(v .* sum(x, 3), 2), 1) + 0.00001 * sum(sum(sum(u .* abs(x(:, :, 2:dn) - x(:, :, 1:dn-1)), 3), 2), 1));
                    subject to
                        sum(x,2) <= 1;
                        sum(x,1) <= 1;
                cvx_end
                l = l + 1;
                fprintf("%d %d: %f %d\n", t, l, cvx_optval, sum(sum(x, 3) > 0.2, 'all'));
            end
            if floor(sum(z)) == 0 && sum(sum(x, 3) > 0.2, 'all') <= n
                break
            end
        end
        fid = fopen(strcat('totalserver_', int2str(numlist(ii)), '_trial_', int2str(jj), '_result.txt'),'wt');
        fprintf(fid,'[');
        for k = 1:n-1
            fprintf(fid,'[');
            for l = 1:m-1
                fprintf(fid,'[');
                fprintf(fid,'%g,',(x(k,l,1:dn-1) > 0.2));
                fprintf(fid,'%g',(x(k,l,dn) > 0.2));
                fprintf(fid,'],');
            end
            fprintf(fid,'[');
            fprintf(fid,'%g,',(x(k,m,1:dn-1) > 0.2));
            fprintf(fid,'%g',(x(k,m,dn) > 0.2));
            fprintf(fid,']');
            fprintf(fid,'],');
        end
        fprintf(fid,'[');
        for l = 1:m-1
            fprintf(fid,'[');
            fprintf(fid,'%g,',(x(n,l,1:dn-1) > 0.2));
            fprintf(fid,'%g',(x(n,l,dn) > 0.2));
            fprintf(fid,'],');
        end
        fprintf(fid,'[');
        fprintf(fid,'%g,',(x(n,m,1:dn-1) > 0.2));
        fprintf(fid,'%g',(x(n,m,dn) > 0.2));
        fprintf(fid,']');
        fprintf(fid,']');
        fprintf(fid,']');
        fclose(fid);
        tocs(ii,jj+1)= toc
    end
end
