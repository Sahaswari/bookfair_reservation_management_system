import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, PieChart, Pie, Cell } from "recharts";
import { Users, LayoutGrid, CheckCircle, AlertCircle } from "lucide-react";
import Header from "@/components/Header";
import { mockStalls } from "@/data/mockData";
import { useEmployeeAuth } from "@/hooks/useEmployeeAuth";

export default function EmployeeDashboard() {
  const { isAuthenticated } = useEmployeeAuth();

  if (!isAuthenticated) {
    return null;
  }
  const totalStalls = mockStalls.length;
  const reservedStalls = mockStalls.filter(s => s.status === 'reserved').length;
  const availableStalls = totalStalls - reservedStalls;
  const occupancyRate = Math.round((reservedStalls / totalStalls) * 100);

  // Stats by size
  const sizeStats = {
    small: {
      total: mockStalls.filter(s => s.size === 'small').length,
      reserved: mockStalls.filter(s => s.size === 'small' && s.status === 'reserved').length,
    },
    medium: {
      total: mockStalls.filter(s => s.size === 'medium').length,
      reserved: mockStalls.filter(s => s.size === 'medium' && s.status === 'reserved').length,
    },
    large: {
      total: mockStalls.filter(s => s.size === 'large').length,
      reserved: mockStalls.filter(s => s.size === 'large' && s.status === 'reserved').length,
    },
  };

  const chartData = [
    { name: 'Small', reserved: sizeStats.small.reserved, available: sizeStats.small.total - sizeStats.small.reserved },
    { name: 'Medium', reserved: sizeStats.medium.reserved, available: sizeStats.medium.total - sizeStats.medium.reserved },
    { name: 'Large', reserved: sizeStats.large.reserved, available: sizeStats.large.total - sizeStats.large.reserved },
  ];

  const pieData = [
    { name: 'Reserved', value: reservedStalls, color: 'hsl(var(--employee))' },
    { name: 'Available', value: availableStalls, color: 'hsl(var(--muted))' },
  ];

  return (
    <div className="min-h-screen bg-background">
      <Header isEmployee />
      
      <div className="container py-8">
        <div className="space-y-8">
          <div>
            <h1 className="text-4xl font-bold mb-2">Organiser Dashboard</h1>
            <p className="text-muted-foreground">
              Overview of CIBF 2025 stall reservations and statistics
            </p>
          </div>

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
                  <div className="font-semibold">Small (3m × 2m)</div>
                  <div className="text-center text-lg font-bold text-employee">{sizeStats.small.reserved}</div>
                  <div className="text-center text-lg font-bold">{sizeStats.small.total - sizeStats.small.reserved}</div>
                </div>
                
                <div className="grid grid-cols-3 gap-4 items-center">
                  <div className="font-semibold">Medium (4m × 3m)</div>
                  <div className="text-center text-lg font-bold text-employee">{sizeStats.medium.reserved}</div>
                  <div className="text-center text-lg font-bold">{sizeStats.medium.total - sizeStats.medium.reserved}</div>
                </div>
                
                <div className="grid grid-cols-3 gap-4 items-center">
                  <div className="font-semibold">Large (6m × 4m)</div>
                  <div className="text-center text-lg font-bold text-employee">{sizeStats.large.reserved}</div>
                  <div className="text-center text-lg font-bold">{sizeStats.large.total - sizeStats.large.reserved}</div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>
  );
}
