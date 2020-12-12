numlist = [10];
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
        for t = max(c)-1:max(d-a)
            tn = min(d, a+t);
            cvx_begin quiet
                variable x(n, m, dn) nonnegative
                expression z(n)
                for k = 1:n
                    z(k) = subplus(c(k) - sum(sum(x(k,:,a(k):tn(k)), 3), 2));
                end
                minimize( sum(z) + 0.001 * sum(sum(sum(x, 3), 2), 1) + 0.00001 * sum(sum(sum(abs(x(:, :, 2:dn) - x(:, :, 1:dn-1)), 3), 2), 1));
                subject to
                    sum(x,2) <= 1
                    sum(x,1) <= 1
            cvx_end
            fprintf("%d 0: %f\n", t, cvx_optval);
            if floor(sum(z)) > 0
                continue
            end
            prev = -1;
            l = 0;
            while abs(cvx_optval-prev) > 0.01
                prev = cvx_optval;
                y = 1 ./ (z + 0.01);
                v = 1 ./ (sum(x, 3) + 0.01);
                u = 1 ./ (abs(x(:, :, 2:dn) - x(:, :, 1:dn-1)) + 0.01);
                cvx_begin quiet
                    variable x(n, m, dn) nonnegative
                    expression z(n)
                    for k = 1:n
                        z(k) = subplus(c(k) - sum(sum(x(k,:,a(k):tn(k)), 3), 2));
                    end
                    minimize( sum(y .* z) + 0.001 * sum(sum(v .* sum(x, 3), 2), 1) + 0.00001 * sum(sum(sum(u .* abs(x(:, :, 2:dn) - x(:, :, 1:dn-1)), 3), 2), 1));
                    subject to
                        sum(x,2) <= 1
                        sum(x,1) <= 1
                cvx_end
                l = l + 1;
                fprintf("%d %d: %f\n", t, l, cvx_optval);
            end
            if floor(sum(z)) == 0
                break
            end
        end
        fid = fopen(strcat('totalserver_', int2str(numlist(ii)), '_trial_', int2str(jj), '_result.txt'),'wt');
        fprintf(fid,'[');
        for k = 1:n-1
            fprintf(fid,'[');
            for l = 1:m-1
                fprintf(fid,'[');
                fprintf(fid,'%g,',round(x(k,l,1:dn-1)));
                fprintf(fid,'%g',round(x(k,l,dn)));
                fprintf(fid,'],');
            end
            fprintf(fid,'[');
            fprintf(fid,'%g,',round(x(k,m,1:dn-1)));
            fprintf(fid,'%g',round(x(k,m,dn)));
            fprintf(fid,']');
            fprintf(fid,'],');
        end
        fprintf(fid,'[');
        for l = 1:m-1
            fprintf(fid,'[');
            fprintf(fid,'%g,',round(x(n,l,1:dn-1)));
            fprintf(fid,'%g',round(x(n,l,dn)));
            fprintf(fid,'],');
        end
        fprintf(fid,'[');
        fprintf(fid,'%g,',round(x(n,m,1:dn-1)));
        fprintf(fid,'%g',round(x(n,m,dn)));
        fprintf(fid,']');
        fprintf(fid,']');
        fprintf(fid,']');
        fclose(fid);
        tocs(ii,jj+1)= toc
    end
end