import { useMemo } from "react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";
import { Users, LayoutGrid, CheckCircle, AlertCircle, Loader2 } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import Header from "@/components/Header";
import { useEmployeeAuth } from "@/hooks/useEmployeeAuth";
import { stallApi, type Stall } from "@/lib/stallApi";

export default function EmployeeDashboard() {
  const { isAuthenticated } = useEmployeeAuth();
  const stallsQuery = useQuery({ queryKey: ["all-stalls"], queryFn: stallApi.listStalls });
  const eventsQuery = useQuery({ queryKey: ["dashboard-events"], queryFn: stallApi.listEvents });

  const stalls = stallsQuery.data ?? [];
  const events = eventsQuery.data ?? [];
  const isLoading = stallsQuery.isLoading || eventsQuery.isLoading;

  const { totalStalls, reservedStalls, availableStalls, sizeStats, pieData } = useMemo(() => {
    if (stalls.length === 0) {
      return {
        totalStalls: 0,
        reservedStalls: 0,
        availableStalls: 0,
        sizeStats: {
          SMALL: { total: 0, reserved: 0 },
          MEDIUM: { total: 0, reserved: 0 },
          LARGE: { total: 0, reserved: 0 },
        },
        pieData: [
          { name: "Reserved", value: 0, color: "hsl(var(--employee))" },
          { name: "Available", value: 0, color: "hsl(var(--muted))" },
        ],
      };
    }

    const bySize: Record<Stall["sizeCategory"], { total: number; reserved: number }> = {
      SMALL: { total: 0, reserved: 0 },
      MEDIUM: { total: 0, reserved: 0 },
      LARGE: { total: 0, reserved: 0 },
    };

    let reserved = 0;
    stalls.forEach((stall) => {
      bySize[stall.sizeCategory].total += 1;
      if (stall.isReserved) {
        reserved += 1;
        bySize[stall.sizeCategory].reserved += 1;
      }
    });

    const total = stalls.length;
    const available = total - reserved;

    return {
      totalStalls: total,
      reservedStalls: reserved,
      availableStalls: available,
      sizeStats: bySize,
      pieData: [
        { name: "Reserved", value: reserved, color: "hsl(var(--employee))" },
        { name: "Available", value: available, color: "hsl(var(--muted))" },
      ],
    };
  }, [stalls]);

  if (!isAuthenticated) {
    return null;
  }
  const occupancyRate = totalStalls ? Math.round((reservedStalls / totalStalls) * 100) : 0;

  const chartData = [
    {
      name: "Small",
      reserved: sizeStats.SMALL.reserved,
      available: sizeStats.SMALL.total - sizeStats.SMALL.reserved,
    },
    {
      name: "Medium",
      reserved: sizeStats.MEDIUM.reserved,
      available: sizeStats.MEDIUM.total - sizeStats.MEDIUM.reserved,
    },
    {
      name: "Large",
      reserved: sizeStats.LARGE.reserved,
      available: sizeStats.LARGE.total - sizeStats.LARGE.reserved,
    },
  ];

  return (
    <div className="min-h-screen bg-background">
      <Header isEmployee />
      
      <div className="container py-8">
        <div className="space-y-8">
          <div>
            <h1 className="text-4xl font-bold mb-2">Organiser Dashboard</h1>
            <p className="text-muted-foreground">
              Overview of live stall reservations and statistics ({events.length} events)
            </p>
          </div>

          {isLoading && (
            <div className="flex items-center gap-2 text-muted-foreground">
              <Loader2 className="h-4 w-4 animate-spin" />
              Loading stall-service data...
            </div>
          )}

          {/* Key Metrics */}
          <div className="grid md:grid-cols-4 gap-4">
            <Card>
              <CardHeader className="pb-2">
                <CardDescription>Total Stalls</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-bold">{totalStalls}</span>
                  <LayoutGrid className="h-8 w-8 text-muted-foreground" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardDescription>Reserved</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-bold text-employee">{reservedStalls}</span>
                  <CheckCircle className="h-8 w-8 text-employee" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardDescription>Available</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-bold text-primary">{availableStalls}</span>
                  <AlertCircle className="h-8 w-8 text-primary" />
                </div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="pb-2">
                <CardDescription>Occupancy Rate</CardDescription>
              </CardHeader>
              <CardContent>
                <div className="flex items-center justify-between">
                  <span className="text-3xl font-bold">{occupancyRate}%</span>
                  <Users className="h-8 w-8 text-muted-foreground" />
                </div>
              </CardContent>
            </Card>
          </div>

          {/* Charts */}
          <div className="grid md:grid-cols-2 gap-6">
            <Card>
              <CardHeader>
                <CardTitle>Occupancy by Stall Size</CardTitle>
                <CardDescription>Reserved vs Available stalls</CardDescription>
              </CardHeader>
              <CardContent>
                <ResponsiveContainer width="100%" height={300}>
                  <BarChart data={chartData}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Bar dataKey="reserved" fill="hsl(var(--employee))" name="Reserved" />
                    <Bar dataKey="available" fill="hsl(var(--muted))" name="Available" />
                  </BarChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle>Overall Occupancy</CardTitle>
                <CardDescription>Total stall distribution</CardDescription>
              </CardHeader>
              <CardContent className="flex items-center justify-center">
                <ResponsiveContainer width="100%" height={300}>
                  <PieChart>
                    <Pie
                      data={pieData}
                      cx="50%"
                      cy="50%"
                      labelLine={false}
                      label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                      outerRadius={100}
                      dataKey="value"
                    >
                      {pieData.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={entry.color} />
                      ))}
                    </Pie>
                    <Tooltip />
                  </PieChart>
                </ResponsiveContainer>
              </CardContent>
            </Card>
          </div>

          {/* Stall Size Breakdown */}
          <Card>
            <CardHeader>
              <CardTitle>Detailed Breakdown by Size</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="space-y-4">
                <div className="grid grid-cols-3 gap-4 text-sm font-medium text-muted-foreground">
                  <div>Stall Size</div>
                  <div className="text-center">Reserved</div>
                  <div className="text-center">Available</div>
                </div>
                
                <div className="grid grid-cols-3 gap-4 items-center">
                  <div className="font-semibold">Small (3m x 2m)</div>
                  <div className="text-center text-lg font-bold text-employee">{sizeStats.SMALL.reserved}</div>
                  <div className="text-center text-lg font-bold">{sizeStats.SMALL.total - sizeStats.SMALL.reserved}</div>
                </div>
                
                <div className="grid grid-cols-3 gap-4 items-center">
                  <div className="font-semibold">Medium (4m x 3m)</div>
                  <div className="text-center text-lg font-bold text-employee">{sizeStats.MEDIUM.reserved}</div>
                  <div className="text-center text-lg font-bold">{sizeStats.MEDIUM.total - sizeStats.MEDIUM.reserved}</div>
                </div>
                
                <div className="grid grid-cols-3 gap-4 items-center">
                  <div className="font-semibold">Large (6m x 4m)</div>
                  <div className="text-center text-lg font-bold text-employee">{sizeStats.LARGE.reserved}</div>
                  <div className="text-center text-lg font-bold">{sizeStats.LARGE.total - sizeStats.LARGE.reserved}</div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
